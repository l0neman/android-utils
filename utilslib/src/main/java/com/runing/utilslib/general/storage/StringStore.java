package com.runing.utilslib.general.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 简单字符串存储工具。
 * <p>
 * 存储路径： Context$FileDir/sd/xx
 */
public class StringStore {

  private static final String TAG = StringStore.class.getSimpleName();

  private static String sDataPath;

  // 保存当前线程正在存储的对象。
  private static ThreadLocal<Store> sStore = new ThreadLocal<Store>() {
    @Override protected Store initialValue() { return new Store(); }
  };

  // 对应的文件读写锁。
  private static Map<String, ReentrantReadWriteLock> sLocks = new ConcurrentHashMap<>();

  // 异步存储线程池。
  private static ExecutorService sWritingService = Executors.newSingleThreadExecutor();
  // 异步读取线程池。
  private static ExecutorService sReadingService = Executors.newCachedThreadPool();
  private static Handler sHandler = new Handler(Looper.getMainLooper());

  /** 初始化 */
  public static void init(Context context) {
    if (sDataPath != null) { return; }

    sDataPath = context.getFilesDir().getPath() + File.separator + "sd";
    File dataDir = new File(sDataPath);
    if (!dataDir.exists()) {
      try {
        //noinspection StatementWithEmptyBody
        if (dataDir.mkdir()) {
//          Log.d(TAG, "data dir create ok: " + sDataPath);
        }
      }
      catch (Exception e) {
//        Log.e(TAG, "data dir create error", e);
      }
    }
  }

  private static String getFilePath(String file) {
    return sDataPath + File.separator + file;
  }

  private static void checkAndCreateFile(String path) {
    File file = new File(path);
    try {
      if (!file.exists()) {
        if (file.createNewFile()) {
          Log.i(TAG, "create file: " + file + " ok.");
        }
      }
    }
    catch (IOException e) {
      Log.e(TAG, "create file: " + file + " error", e);
    }
  }

  public static final class Store {
    private String file;
    private String path;

    private void setFile(String file) {
      this.file = file;
      this.path = getFilePath(file);
      checkAndCreateFile(this.path);
    }

    public void write(String content) {
      final ReentrantReadWriteLock lock = sLocks.get(file);
      if (lock != null) {
        lock.writeLock().lock();
      }
      try {
        writeToFile(path, content);
      }
      catch (IOException e) {
        Log.e(TAG, "save file: " + file + "error", e);
      }
      finally {
        if (lock != null) {
          lock.writeLock().unlock();
        }
      }
    }

    public void writeAsync(final String content) {
      final ReentrantReadWriteLock lock = sLocks.get(file);
      sWritingService.execute(new Runnable() {
        @Override public void run() {
          if (lock != null) {
            lock.writeLock().lock();
          }
          SystemClock.sleep(4 * 1000);
          try {
            writeToFile(path, content);
          }
          catch (IOException e) {
//            Log.e(TAG, "save file: " + file + "error", e);
          }
          finally {
            if (lock != null) {
              lock.writeLock().unlock();
            }
          }
        }
      });
    }
  }

  public static Store open(final String file) {
    final Store store = sStore.get();
    if (store == null) {
      throw new NullPointerException("open error: " + file);
    }
    store.setFile(file);
    if (!sLocks.containsKey(file)) {
      sLocks.put(file, new ReentrantReadWriteLock());
    }
    return store;
  }

  public static String read(String file) {
    String fullPath = getFilePath(file);
    final ReentrantReadWriteLock lock = sLocks.get(file);
    if (lock != null) {
      lock.readLock().lock();
    }
    try {
      return readFromFile(fullPath);
    }
    catch (IOException e) {
//      Log.e(TAG, "read file: " + file + "error", e);
      return null;
    }
    finally {
      if (lock != null) {
        lock.readLock().unlock();
      }
    }
  }

  public interface ReadCallback {
    void onRead(String content);
  }

  public static void readAsync(final String file, final ReadCallback callback) {
    final String fullPath = getFilePath(file);
    final ReentrantReadWriteLock lock = sLocks.get(file);
    sReadingService.execute(new Runnable() {
      @Override public void run() {
        if (lock != null) {
          lock.readLock().lock();
        }
        String content = null;

        try {
          content = readFromFile(fullPath);
        }
        catch (IOException e) {
//          Log.e(TAG, "read file: " + file + "error", e);
        }
        finally {
          if (lock != null) {
            lock.readLock().unlock();
          }
        }

        final String copy = content;
        sHandler.post(new Runnable() {
          @Override public void run() {
            callback.onRead(copy);
          }
        });
      }
    });
  }

  public static void delete(String file) {
    sWritingService.execute(new Runnable() {
      @Override public void run() {
        final ReentrantReadWriteLock lock = sLocks.get(file);
        if (lock != null) {
          lock.writeLock().lock();
        }
        try {
          //noinspection StatementWithEmptyBody
          if (new File(getFilePath(file)).delete()) {
//        Log.i(TAG, "delete file: " + file + " ok.");
          }
        }
        catch (Exception e) {
//      Log.e(TAG, "delete file: " + file, e);
        }
        if (lock != null) {
          lock.writeLock().unlock();
        }
        // 移除锁缓存。
        sLocks.remove(file);
      }
    });
  }

  public static void deleteAll() {
    sWritingService.execute(new Runnable() {
      @Override public void run() {
        final Set<Map.Entry<String, ReentrantReadWriteLock>> entries = sLocks.entrySet();
        for (Map.Entry<String, ReentrantReadWriteLock> entry : entries) {
          entry.getValue().writeLock().lock();
          final String file = entry.getKey();
          try {
            //noinspection StatementWithEmptyBody
            if (new File(getFilePath(file)).delete()) {
//          Log.i(TAG, "delete file: " + file + " ok.");
            }
          }
          catch (Exception e) {
//        Log.e(TAG, "delete file: " + file, e);
          }
          entry.getValue().writeLock().unlock();
        }
        sLocks.clear();
      }
    });
  }

  // utils:
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
