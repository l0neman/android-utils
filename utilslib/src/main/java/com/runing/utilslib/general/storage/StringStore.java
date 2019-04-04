package com.runing.utilslib.general.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单字符串存储工具。
 * <p>
 * 默认存储路径： Context$FileDir/ss/
 * <p>
 * Created by l0neman on 2019/04/04.
 */
public class StringStore implements IStringStore {
  private static final String TAG = StringStore.class.getSimpleName();

  /* 调试开关 */
  private static final boolean DEBUG = false;
  private String mDataPath;

  // 保存当前线程正在存储的对象。
  private ThreadLocal<Store> sStore = new ThreadLocal<Store>() {
    @Override protected Store initialValue() { return new Store(); }
  };

  private Map<String, AtomicInteger> sWriteLocks = new ConcurrentHashMap<>();
  private Map<String, AtomicInteger> sReadLocks = new ConcurrentHashMap<>();
  private Map<String, Object> sLocks = new ConcurrentHashMap<>();

  // 异步存储线程池。
  private ExecutorService sWritingService = Executors.newSingleThreadExecutor();
  // 异步读取线程池。
  private ExecutorService sReadingService = Executors.newCachedThreadPool();
  private Handler sHandler = new Handler(Looper.getMainLooper());

  public StringStore(Context context) {
    this(context.getFilesDir().getPath() + File.separator + "sd");
  }

  public StringStore(String dataPath) {
    this.mDataPath = dataPath;
    checkAndCreateDir(dataPath);
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为成员变量。
  public class Store implements IStringStore.StringStore {
    private String name;
    private String path;

    private void setFile(String name) {
      this.name = name;
      this.path = getFilePath(name);
      checkAndCreateFile(this.path);
    }

    @Override public void write(String value) {
      // get lock.
      final Object lock = sLocks.get(name);
      AtomicInteger readLock = sReadLocks.get(name);
      AtomicInteger writeLock = sWriteLocks.get(name);

      if (lock == null || writeLock == null || readLock == null) {
        throw new AssertionError("unexpected error.");
      }

      // wait read threads.
      synchronized (lock) {
        while (readLock.get() != 0) {
          try {
            lock.wait();
          }
          catch (InterruptedException e) {
            if (DEBUG) {
              Log.i(TAG, "write interrupted.");
            }
          }
        }
      }
      writeLock.getAndIncrement();
      try {
        writeToFile(path, value);
      }
      catch (IOException e) {
        if (DEBUG) {
          Log.e(TAG, "save file: " + name + "error", e);
        }
      }
      finally {
        writeLock.getAndDecrement();
        // notify read threads.
        if (writeLock.get() == 0) {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    }

    @Override public void writeAsync(final String value) {
      // get lock.
      final Object lock = sLocks.get(name);
      final AtomicInteger readLock = sReadLocks.get(name);
      final AtomicInteger writeLock = sWriteLocks.get(name);

      if (lock == null || writeLock == null || readLock == null) {
        throw new AssertionError("unexpected error.");
      }

      sWritingService.execute(new Runnable() {
        @Override public void run() {
          // wait read threads.
          synchronized (lock) {
            while (readLock.get() != 0) {
              try {
                lock.wait();
              }
              catch (InterruptedException e) {
                if (DEBUG) {
                  Log.i(TAG, "writeAsync interrupted.");
                }
              }
            }
          }
          writeLock.getAndIncrement();
          try {
            writeToFile(path, value);
          }
          catch (IOException e) {
            if (DEBUG) {
              Log.e(TAG, "save file: " + name + "error", e);
            }
          }
          finally {
            writeLock.getAndDecrement();
            // notify read threads.
            if (writeLock.get() == 0) {
              synchronized (lock) {
                lock.notifyAll();
              }
            }
          }
        }
      });
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为成员变量。
    @Override public String read() {
      checkAndSetLock(name);
      // get lock.
      final Object lock = sLocks.get(name);
      AtomicInteger writeLock = sWriteLocks.get(name);
      AtomicInteger readLock = sReadLocks.get(name);

      if (lock == null || writeLock == null || readLock == null) {
        throw new AssertionError("unexpected error.");
      }

      // wait write threads.
      synchronized (lock) {
        while (writeLock.get() != 0) {
          try {
            lock.wait();
          }
          catch (InterruptedException e) {
            if (DEBUG) {
              Log.i(TAG, "read interrupted.");
            }
          }
        }
      }
      readLock.getAndIncrement();
      try {
        return readFromFile(path);
      }
      catch (IOException e) {
        if (DEBUG) {
          Log.e(TAG, "read file: " + name + "error", e);
        }
        return null;
      }
      finally {
        readLock.getAndDecrement();
        // notify write threads.
        if (readLock.get() == 0) {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    }

    @Override public void readAsync(final ReadCallback<String> callback) {
      checkAndSetLock(name);
      // get lock.
      final Object lock = sLocks.get(name);
      final AtomicInteger writeLock = sWriteLocks.get(name);
      final AtomicInteger readLock = sReadLocks.get(name);

      if (lock == null || writeLock == null || readLock == null) {
        throw new AssertionError("unexpected error.");
      }

      sReadingService.execute(new Runnable() {
        @Override public void run() {
          // wait write threads.
          synchronized (lock) {
            while (writeLock.get() != 0) {
              try {
                lock.wait();
              }
              catch (InterruptedException e) {
                if (DEBUG) {
                  Log.i(TAG, "readAsync interrupted.");
                }
              }
            }
          }

          readLock.getAndIncrement();
          String content = null;
          try {
            content = readFromFile(path);
          }
          catch (IOException e) {
            if (DEBUG) {
              Log.e(TAG, "read file: " + name + "error", e);
            }
          }
          finally {
            readLock.getAndDecrement();
            // notify write reads.
            if (readLock.get() == 0) {
              synchronized (lock) {
                lock.notifyAll();
              }
            }
          }

          final String copy = content;
          sHandler.post(new Runnable() {
            @Override public void run() {
              callback.onValue(copy);
            }
          });
        }
      });
    }
  }

  private String getFilePath(String file) {
    return mDataPath + File.separator + file;
  }

  @Override public StringStore open(String file) {
    final Store store = sStore.get();
    if (store == null) {
      throw new NullPointerException("open error: " + file);
    }
    store.setFile(file);
    checkAndSetLock(file);
    return store;
  }

  private void checkAndSetLock(String file) {
    if (!sLocks.containsKey(file)) {
      sLocks.put(file, new Object());
    }
    if (!sWriteLocks.containsKey(file)) {
      sWriteLocks.put(file, new AtomicInteger(0));
    }
    if (!sReadLocks.containsKey(file)) {
      sReadLocks.put(file, new AtomicInteger(0));
    }
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为成员变量。
  @Override public void delete(final String file) {
    checkAndSetLock(file);
    // get lock.
    final Object lock = sLocks.get(file);
    final AtomicInteger writeLock = sWriteLocks.get(file);
    final AtomicInteger readLock = sReadLocks.get(file);

    if (lock == null || writeLock == null || readLock == null) {
      throw new AssertionError("unexpected error.");
    }

    sWritingService.execute(new Runnable() {
      @Override public void run() {
        synchronized (lock) {
          // wait read and write threads.
          while (writeLock.get() != 0 || readLock.get() != 0) {
            try {
              lock.wait();
            }
            catch (InterruptedException e) {
              if (DEBUG) {
                Log.i(TAG, "delete interrupted.");
              }
            }
          }
        }
        final File target = new File(getFilePath(file));
        if (!target.exists()) { return; }

        try {
          if (target.delete()) {
            if (DEBUG) {
              Log.i(TAG, "delete file: " + file + " ok.");
            }
          }
        }
        catch (Exception e) {
          if (DEBUG) {
            Log.e(TAG, "delete file: " + file, e);
          }
        }
        sLocks.remove(file);
      }
    });
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为全局变量。
  @Override public void deleteAll() {
    sWritingService.execute(new Runnable() {
      @Override public void run() {
        final File dir = new File(mDataPath);
        if (!dir.exists() || !dir.isDirectory()) {
          return;
        }
        final File[] files = dir.listFiles();
        if (files.length == 0) {
          return;
        }

        for (File file : files) {
          final String name = file.getName();
          // get lock.
          final Object lock = sLocks.get(name);
          final AtomicInteger writeLock = sWriteLocks.get(name);
          final AtomicInteger readLock = sReadLocks.get(name);

          if (lock == null || writeLock == null || readLock == null) {
            throw new AssertionError("unexpected error.");
          }

          synchronized (lock) {
            // wait read and write threads.
            while (writeLock.get() != 0 || readLock.get() != 0) {
              try {
                lock.wait();
              }
              catch (InterruptedException e) {
                if (DEBUG) {
                  Log.i(TAG, "deleteAll interrupted.");
                }
              }
            }
          }

          if (file.delete()) {
            if (DEBUG) {
              Log.i(TAG, "delete file: " + file + " ok.");
            }
          }
        }
      }
    });
  }

  // file utils:

  private static void checkAndCreateFile(String path) {
    File file = new File(path);
    try {
      if (!file.exists()) {
        if (file.createNewFile()) {
          if (DEBUG) {
            Log.i(TAG, "create file: " + file + " ok.");
          }
        }
      }
    }
    catch (IOException e) {
      if (DEBUG) {
        Log.e(TAG, "create file: " + file + " error", e);
      }
    }
  }

  private static void checkAndCreateDir(String dirPath) {
    File dataDir = new File(dirPath);
    if (!dataDir.exists()) {
      try {
        if (dataDir.mkdir()) {
          if (DEBUG) {
            Log.d(TAG, "data dir create ok: " + dirPath);
          }
        }
      }
      catch (Exception e) {
        if (DEBUG) {
          Log.e(TAG, "data dir create error", e);
        }
      }
    }
  }

  // io utils:

  private static void writeToFile(String file, String content) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(file);
      writer.write(content);
    }
    finally {
      closeQuietly(writer);
    }
  }

  private static String readFromFile(String file) throws IOException {
    BufferedReader br = null;
    try {
      StringBuilder builder = new StringBuilder();
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        builder.append(line);
      }
      return builder.toString();
    }
    finally {
      closeQuietly(br);
    }
  }

  private static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
