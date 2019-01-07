package com.runing.utilslib.net;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Utils Interface (Contains the most basic request method).
 * <p>
 * Created by runing in 2018.12.27
 */
public interface HttpUtils {

  /** Request exception basic type */
  class HttpException extends Exception {
    public HttpException(Throwable cause) { super(cause); }

    public HttpException(String message) { super(message); }

    public HttpException(String message, Throwable cause) { super(message, cause); }
  }

  /** Request listener */
  interface Listener {
    /** request ok, callback result original content */
    void onSucceed(Task task);

    /** request error, callback exception */
    void onFailed(Task task, HttpException e);
  }

  /** Download method listener */
  interface DownloadListener extends Listener {

    /** download progress (0 - 100) */
    void onProgress(int progress);
  }

  /** Cache control interface */
  interface Cache {
    /** cache file dir */
    String cacheDir();

    /** cache size (byte) */
    long cacheSize();

    /** clear cache dir */
    void clear();
  }

  /** string to type interface */
  interface Converter<T> {

    /** convert string to type */
    T convert(String result) throws Exception;
  }

  /**
   * request task interface.
   * <p>
   * It is thread safe.
   */
  interface Task {
    /**
     * Convert result string to a type if the result string is not null.
     *
     * @param converter {@link Converter}.
     * @param <T>       target type.
     * @return target value.
     *
     * @throws Exception convert error.
     */
    <T> T result(Converter<T> converter) throws Exception;

    /** Request result string, It will be assigned after the request is completed */
    String result();

    /** Download result file, It will be assigned after the download is completed */
    File file();

    /** Download progress */
    int progress();

    /** HTTP response code */
    int code();

    /** Cancel request */
    void cancel();
  }

  /** HttpUtils builder */
  interface Builder {
    /** Set connection time out */
    Builder connTimeOut(long time, TimeUnit timeUnit);

    /** Set read time out */
    Builder readTimeOut(long time, TimeUnit timeUnit);

    /** Set response cache dir */
    Builder cacheDir(File dir);

    /** Set response cache max size (byte) */
    Builder cacheSize(long bytes);

    /** build a new {@link HttpUtils} instance */
    HttpUtils build();
  }

  /** Cache control, {@link Cache} */
  Cache cache();

  /** Set request url, it must be called first */
  HttpUtils url(String url);

  /**
   * Set request url, it must be called first.
   *
   * @param url       request url.
   * @param urlParams url suffix params.
   * @return for chains call.
   */
  HttpUtils url(String url, String[] urlParams);

  /** Set cache seconds */
  HttpUtils cache(int seconds);

  /** Set json params */
  HttpUtils params(String json);

  /** Set json or urlencoded params */
  HttpUtils params(String[] params);

  /** Set post params, support json only */
  HttpUtils params(Object[] params);

  /** Set json params */
  HttpUtils params(JSONObject jsonParam);

  /** Set request headers */
  HttpUtils header(String[] headers);

  /** Set request type is json */
  HttpUtils json();

  /** Set request type is json */
  HttpUtils urlencoded();

  /** Set request type is multi-part */
  HttpUtils multiPart();

  /** Download to target file (Sync method) */
  Task download(File target) throws HttpException;

  /**
   * Download to target (Async method).
   *
   * @param target   target file.
   * @param listener download listener {@link DownloadListener}.
   * @return Request task {@link Task}.
   */
  Task download(File target, DownloadListener listener);

  /** HTTP get request (Sync) */
  Task get() throws HttpException;

  /**
   * HTTP post request (Async).
   *
   * @param listener request listener {@link Listener}
   * @return Request {@link Task}.
   */
  Task get(Listener listener);

  /** HTTP post request (Sync) */
  Task post() throws HttpException;

  /**
   * HTTP get request (Async).
   *
   * @param listener request listener {@link Listener}
   * @return Request task {@link Task}.
   */
  Task post(Listener listener);
}
