package io.l0neman.utils.general.storage;

import java.io.IOException;

/**
 * Created by l0neman on 2019/04/24.
 * <p>
 * 基于目录的存储接口。
 */
public interface IDirStore {

  interface ErrorCallback {
    void onError(IOException e);
  }

  /** 异步存储错误回调 */
  interface WriteCallback extends ErrorCallback {}

  /**
   * 异步读取回调。
   *
   * @param <T> 数据类型。
   */
  interface ReadCallback<T> extends ErrorCallback {
    /** 读取结果 */
    void onValue(T value);
  }

  /**
   * 文件输入输出适配器。
   *
   * @param <T> 输入输出数据类型。
   */
  interface FileIOAdapter<T> {
    /** 提供类型 */
    Class<T> typeToken();

    /** 向文件写入 */
    void write(String file, T value) throws IOException;

    /** 从文件读取 */
    T read(String file) throws IOException;
  }

  /**
   * 基于文件的存储接口。
   *
   * @param <T>
   */
  interface FileStore<T> {

    /** 同步保存数据 */
    void write(T value) throws IOException;

    /** 异步保存数据 */
    void writeAsync(T value, WriteCallback callback);

    /** 同步读取数据 */
    T read() throws IOException;

    /** 异步读取数据 */
    void readAsync(ReadCallback<T> callback);

    /** 设置 IO 适配器 */
    void setAdapter(FileIOAdapter<T> adapter);
  }

  /**
   * 获得一个文件存储。
   *
   * @param adapter  类型存储适配器。
   * @param fileName 存储文件名。
   * @param <T>      存储数据类型。
   * @return 返回文件存储对象。
   */
  <T> IDirStore.FileStore<T> with(String fileName, FileIOAdapter<T> adapter);

  /**
   * 删除目录中的文件。
   *
   * @param fileName 文件名。
   */
  void deleteFile(String fileName);

  /** 删除整个目录 */
  void deleteSelf();
}
