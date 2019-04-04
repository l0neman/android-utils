package com.runing.utilslib.general.storage;

/**
 * Created by l0neman on 2019/04/04.
 */
public interface IStore<T> {

  interface ReadCallback<T> {
    void onValue(T value);
  }

  interface Store<T> {
    void write(T value);

    void writeAsync(T value);

    T read();

    void readAsync(ReadCallback<T> callback);
  }

  Store<T> open(String file);

  void delete(String file);

  void deleteAll();
}
