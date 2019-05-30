package io.l0neman.utils.general.storage;

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
 * Created by l0neman on 2019/04/24.
 * <p>
 * DirStore 实现类，目前只提供了 String 的 FileIOAdapter 类型。
 */
public class DirStore implements IDirStore {

  private static final String TAG = DirStore.class.getSimpleName();

  /* 调试开关 */
  private static final boolean DEBUG = false;
  private String mDataPath;

  private Map<Class<?>, FileStoreThreadLocal<?>> sCacheFileStore = new ConcurrentHashMap<>();

  private final class FileStoreThreadLocal<T> extends ThreadLocal<FileStore<T>> {
    @Override protected FileStore<T> initialValue() {
      return new FileStore<>();
    }
  }

  private Map<String, AtomicInteger> sWriteLocks = new ConcurrentHashMap<>();
  private Map<String, AtomicInteger> sReadLocks = new ConcurrentHashMap<>();
  private Map<String, Object> sLocks = new ConcurrentHashMap<>();

  // 异步存储线程池。
  private ExecutorService sWritingService = Executors.newSingleThreadExecutor();
  // 异步读取线程池。
  private ExecutorService sReadingService = Executors.newCachedThreadPool();
  private Handler sHandler = new Handler(Looper.getMainLooper());

  public DirStore(Context context) {
    this(context.getFilesDir().getPath() + File.separator + "ds");
  }

  public DirStore(String dataPath) {
    this.mDataPath = dataPath;
    checkAndCreateDir(dataPath);
  }

  private String getFilePath(String file) {
    return mDataPath + File.separator + file;
  }

  public final class FileStore<T> implements IDirStore.FileStore<T> {

    private String name;
    private String path;
    private FileIOAdapter<T> adapter;

    private void init(String fileName, FileIOAdapter<T> adapter) {
      this.name = fileName;
      this.path = getFilePath(fileName);
      setAdapter(adapter);
      checkAndCreateFile(this.path);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为成员变量。
    @Override public void write(final T value) throws IOException {
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
          } catch (InterruptedException e) {
            if (DEBUG) {
              Log.i(TAG, "write interrupted.");
            }
          }
        }
      }

      writeLock.getAndIncrement();

      try {
        adapter.write(path, value);
      } finally {
        writeLock.getAndDecrement();
        // notify read threads.
        if (writeLock.get() == 0) {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    }

    @Override public void writeAsync(final T value, WriteCallback callback) {
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
              } catch (InterruptedException e) {
                if (DEBUG) {
                  Log.i(TAG, "writeAsync interrupted.");
                }
              }
            }
          }

          writeLock.getAndIncrement();

          try {
            adapter.write(path, value);
          } catch (IOException e) {
            if (callback != null) {
              if (DEBUG) {
                Log.e(TAG, "write file: " + name + "error", e);
              }

              sHandler.post(new Runnable() {
                @Override public void run() {
                  callback.onError(e);
                }
              });
            }
          } finally {
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
    @Override public T read() throws IOException {
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
          } catch (InterruptedException e) {
            if (DEBUG) {
              Log.i(TAG, "read interrupted.");
            }
          }
        }
      }

      readLock.getAndIncrement();
      try {
        return adapter.read(path);
      } finally {
        readLock.getAndDecrement();
        // notify write threads.
        if (readLock.get() == 0) {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      }
    }

    @Override public void readAsync(ReadCallback<T> callback) {
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
              } catch (InterruptedException e) {
                if (DEBUG) {
                  Log.i(TAG, "readAsync interrupted.");
                }
              }
            }
          }

          readLock.getAndIncrement();
          T content = null;
          try {
            content = adapter.read(path);

          } catch (IOException e) {
            if (DEBUG) {
              Log.e(TAG, "read file: " + name + "error", e);
            }

            sHandler.post(new Runnable() {
              @Override public void run() {
                callback.onError(e);
              }
            });
          } finally {
            readLock.getAndDecrement();
            // notify write reads.
            if (readLock.get() == 0) {
              synchronized (lock) {
                lock.notifyAll();
              }
            }
          }

          final T copy = content;
          sHandler.post(new Runnable() {
            @Override public void run() {
              callback.onValue(copy);
            }
          });
        }
      });
    }

    @Override public void setAdapter(FileIOAdapter<T> adapter) {
      this.adapter = adapter;
    }

  }

  @Override public <T> FileStore<T> with(String fileName, FileIOAdapter<T> adapter) {
    @SuppressWarnings("unchecked")
    FileStoreThreadLocal<T> storeThreadLocal = (FileStoreThreadLocal<T>)
        sCacheFileStore.get(adapter.typeToken());

    if (storeThreadLocal == null) {
      storeThreadLocal = new FileStoreThreadLocal<>();
      // 针对每个类型的文件存储做线程缓存。
      sCacheFileStore.put(adapter.typeToken(), storeThreadLocal);
    }

    FileStore<T> fileStore = storeThreadLocal.get();

    if (fileStore == null) {
      throw new NullPointerException("with error: " + fileName);
    }

    fileStore.init(fileName, adapter);
    // 针对每个文件名做不同的线程锁。
    checkAndSetLock(fileName);
    return fileStore;
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

  @Override public void deleteFile(String fileName) {
    checkAndSetLock(fileName);
    // get lock.
    final Object lock = sLocks.get(fileName);
    final AtomicInteger writeLock = sWriteLocks.get(fileName);
    final AtomicInteger readLock = sReadLocks.get(fileName);

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
            } catch (InterruptedException e) {
              if (DEBUG) {
                Log.i(TAG, "delete interrupted.");
              }
            }
          }
        }

        final File target = new File(getFilePath(fileName));
        if (!target.exists()) { return; }

        try {
          if (target.delete()) {
            if (DEBUG) {
              Log.i(TAG, "delete file: " + fileName + " ok.");
            }
          }
        } catch (Exception e) {
          if (DEBUG) {
            Log.e(TAG, "delete file: " + fileName, e);
          }
        }

        sLocks.remove(fileName);
      }
    });
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // lock 为成员变量。
  @Override public void deleteSelf() {
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
              } catch (InterruptedException e) {
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

  public static FileIOAdapter<String> STRING_IO_ADAPTER = new FileIOAdapter<String>() {
    @Override public Class<String> typeToken() {
      return String.class;
    }

    @Override public void write(String file, String value) throws IOException {
      FileWriter writer = null;
      try {
        writer = new FileWriter(file);
        writer.write(value);

      } finally {
        closeQuietly(writer);
      }
    }

    @Override public String read(String file) throws IOException {
      BufferedReader br = null;
      try {
        StringBuilder builder = new StringBuilder();
        br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
          builder.append(line);
        }
        return builder.toString();

      } finally {
        closeQuietly(br);
      }
    }
  };

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
    } catch (IOException e) {
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
      } catch (Exception e) {
        if (DEBUG) {
          Log.e(TAG, "data dir create error", e);
        }
      }
    }
  }

  // io utils:

  private static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException ignore) {
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
