package com.runing.utilslib.net;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface HttpUtils {

  interface Task<T> {
    T get();

    boolean finished();

    void cancel();
  }

  interface Cache {
    String directory();

    String size();

    String clear();
  }

  interface Cookie {
    Map<String, String> all();

    String get(String key);
  }

  HttpUtils get();

  HttpUtils post();

  HttpUtils json();

  Cookie cookie();

  HttpUtils cookie(String key, String value);

  HttpUtils urlencoded();

  HttpUtils multipart();

  HttpUtils maxAge(long time, TimeUnit unit);

  HttpUtils maxStale(long time, TimeUnit unit);

  HttpUtils param(String key, int value);

  HttpUtils param(String key, long value);

  HttpUtils param(String key, float value);

  HttpUtils param(String key, double value);

  HttpUtils param(String key, boolean value);

  HttpUtils param(String key, String value);

  HttpUtils header(String key, String value);

  interface Callback<T> {
    void onSuccess(T result);

    void onError(Exception e);
  }

  interface OnProgress {
    void onProgress(int progress, byte[] buffer);

    void onSuccess();

    void onError(Exception e);
  }

  Task request(Callback callback);

  Task request();

  Task download(OnProgress onProgress);

  Task download();
}
