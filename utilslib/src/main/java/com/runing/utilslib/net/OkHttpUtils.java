package com.runing.utilslib.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HttpUtils implement with OkHttp.
 * <p>
 * Created by runing in 2018.12.27
 */
public class OkHttpUtils implements HttpUtils {

  /* post multi-part file */
  private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");
  /* post json */
  private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

  private Cache mCache;
  private OkCallBuilder mOkCallBuilder;
  private ExecutorService mExec = Executors.newCachedThreadPool();
  private Handler mHandler = new Handler(Looper.getMainLooper());

  private OkHttpUtils(OkHttpClient coreClient, Cache mCache) {
    this.mCache = mCache;
    mOkCallBuilder = new OkCallBuilder(coreClient);
  }

  public static final class Builder implements HttpUtils.Builder {
    private long connTimeOut = 10;
    private TimeUnit connTimeOutTimeUnit = TimeUnit.SECONDS;
    private long readTimeOut = 10;
    private TimeUnit readTimeOutTimeUnit = TimeUnit.SECONDS;
    private long cacheSize;
    private File cacheDir;
    private CookieJar cookieJar;

    public Builder cookieJar(CookieJar cookieJar) {
      this.cookieJar = cookieJar;
      return this;
    }

    @Override public Builder connTimeOut(long time, TimeUnit timeUnit) {
      this.connTimeOutTimeUnit = timeUnit;
      this.connTimeOut = time;
      return this;
    }

    @Override public Builder readTimeOut(long time, TimeUnit timeUnit) {
      this.readTimeOutTimeUnit = timeUnit;
      this.readTimeOut = time;
      return this;
    }

    @Override public Builder cacheSize(long bytes) {
      this.cacheSize = bytes;
      return this;
    }

    @Override public Builder cacheDir(File dir) {
      this.cacheDir = dir;
      return this;
    }

    @Override public HttpUtils build() {
      final OkHttpClient.Builder builder = new OkHttpClient.Builder();
      if (cookieJar != null) { builder.cookieJar(cookieJar); }

      if (cacheDir != null && cacheSize != 0) {
        builder.cache(new okhttp3.Cache(cacheDir, cacheSize));
      }

      OkHttpClient coreClient = builder
          .connectTimeout(connTimeOut, connTimeOutTimeUnit)
          .readTimeout(readTimeOut, readTimeOutTimeUnit)
          .build();
      Cache cache = new Cache(cacheDir.getPath());
      return new OkHttpUtils(coreClient, cache);
    }
  }

  public static final class Cache implements HttpUtils.Cache {

    private final String cacheDir;

    private Cache(String cacheDir) {
      this.cacheDir = cacheDir;
    }

    @Override public String cacheDir() {
      return cacheDir;
    }

    private static long getFileSize(String path) {
      File file = new File(path);
      if (!file.exists()) {
        return 0L;
      }
      if (file.isFile()) {
        return file.length();
      }
      if (file.isDirectory()) {
        int size = 0;
        File[] files = file.listFiles();
        for (int i = files.length - 1; i >= 0; i--) {
          size += getFileSize(files[i].getAbsolutePath());
        }
        return size;
      }
      return 0L;
    }

    @Override public long cacheSize() {
      return getFileSize(cacheDir);
    }

    private static void delete(String path) {
      File file = new File(path);
      if (!file.exists()) {
        return;
      }
      if (file.isFile()) {
        //noinspection ResultOfMethodCallIgnored
        file.delete();
      }
      else if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (int i = files.length - 1; i >= 0; i--) {
          delete(files[i].getAbsolutePath());
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
      }
    }

    @Override public void clear() {
      delete(cacheDir);
    }
  }

  public static final class Task implements HttpUtils.Task {
    private Call mCall;
    private String result;
    private File file;
    private int progress;
    private int code;

    private Task(Call mCall) {
      this.mCall = mCall;
    }

    private void setResult(String result) {
      this.result = result;
    }

    private void setFile(File file) {
      this.file = file;
    }

    private void setProgress(int progress) {
      this.progress = progress;
    }

    private void setCode(int code) {
      this.code = code;
    }

    @Override public <T> T result(Converter<T> converter) throws Exception {
      return converter.convert(result);
    }

    @Override public String result() {
      return result;
    }

    @Override public File file() {
      return file;
    }

    @Override public int progress() {
      return progress;
    }

    @Override public int code() {
      return code;
    }

    @Override public void cancel() {
      mCall.cancel();
    }

    @Override public String toString() {
      return "Task{" +
          "result='" + result + '\'' +
          ", file=" + file +
          ", progress=" + progress +
          ", code=" + code +
          '}';
    }
  }

  private static final class OkCallBuilder {
    private OkHttpClient okHttpClient;
    private Request.Builder builder = new Request.Builder();
    private String jsonString;
    private boolean isJson;
    private boolean isGet;
    private String[] encodedParams = new String[0];
    private Object[] multiPartParams = new Object[0];
    private boolean isUrlencoded;
    private boolean isMultiPart;

    OkCallBuilder(OkHttpClient okHttpClient) {
      this.okHttpClient = okHttpClient;
    }

    private void clear() {
      this.builder = new Request.Builder();
      this.jsonString = null;
      this.isJson = false;
      this.isGet = false;
      this.encodedParams = new String[0];
      this.multiPartParams = new Object[0];
      this.isUrlencoded = false;
      this.isMultiPart = false;
    }

    OkCallBuilder url(String url) {
      clear();
      builder.url(url);
      return this;
    }

    OkCallBuilder get() {
      this.isGet = true;
      return this;
    }

    OkCallBuilder cache(int seconds) {
      builder.cacheControl(new CacheControl.Builder()
          .maxStale(seconds, TimeUnit.SECONDS).build());
      return this;
    }

    OkCallBuilder params(String json) {
      jsonString = json;
      return this;
    }

    static String getJsonString(Object[] param) {
      if (param == null || param.length == 0) {
        return "";
      }
      JSONObject json = new JSONObject();
      for (int i = 0; i < param.length; i += 2) {
        try {
          json.put((String) param[i], param[i + 1]);
        }
        catch (JSONException ignore) {}
      }
      return json.toString();
    }

    OkCallBuilder params(String[] params) {
      if (isJson) {
        this.jsonString = getJsonString(params);
      }
      else if (isUrlencoded) {
        this.encodedParams = params;
      }
      else {
        this.multiPartParams = params;
      }
      return this;
    }

    OkCallBuilder params(Object[] params) {
      if (isJson) {
        this.jsonString = getJsonString(params);
      }
      else {
        this.multiPartParams = params;
      }
      return this;
    }

    OkCallBuilder params(JSONObject jsonParam) {
      if (isJson) {
        this.jsonString = jsonParam.toString();
      }
      return this;
    }

    OkCallBuilder header(String[] headers) {
      for (int i = 0; i < headers.length; i += 2) {
        builder.header(headers[i], headers[i + 1]);
      }
      return this;
    }

    OkCallBuilder json() {
      this.isJson = true;
      return this;
    }

    OkCallBuilder urlencoded() {
      this.isUrlencoded = true;
      return this;
    }

    OkCallBuilder multiPart() {
      this.isMultiPart = true;
      return this;
    }

    Call build() {
      Log.d("HttpUtilsTest", "build: " + toString());
      if (isGet) {
        this.builder.get();
      }
      if (isJson) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, jsonString);
        this.builder.post(requestBody);
      }
      else if (isUrlencoded) {
        FormBody.Builder builder = new FormBody.Builder();
        for (int i = 0; i < encodedParams.length; i += 2) {
          builder.add(encodedParams[i], encodedParams[i + 1]);
        }
        this.builder.post(builder.build());
      }
      else if (isMultiPart) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MultipartBody.FORM);
        for (int i = 0; i < multiPartParams.length; i += 2) {
          if (multiPartParams[i + 1] instanceof File) {
            File file = (File) multiPartParams[i + 1];
            builder.addFormDataPart((String) multiPartParams[i],
                file.getName(), RequestBody.create(MEDIA_TYPE_FILE, file));
          }
          else {
            builder.addFormDataPart((String) multiPartParams[i], (String) multiPartParams[i + 1]);
          }
        }
        this.builder.post(builder.build());
      }
      return okHttpClient.newCall(builder.build());
    }

    @Override public String toString() {
      return "OkCallBuilder{" +
          "jsonString='" + jsonString + '\'' +
          ", isJson=" + isJson +
          ", isGet=" + isGet +
          ", encodedParams=" + Arrays.toString(encodedParams) +
          ", multiPartParams=" + Arrays.toString(multiPartParams) +
          ", isUrlencoded=" + isUrlencoded +
          ", isMultiPart=" + isMultiPart +
          '}';
    }
  }

  @Override public Cache cache() {
    return mCache;
  }

  @Override public HttpUtils url(String url) {
    mOkCallBuilder.url(url);
    return this;
  }

  @Override public HttpUtils url(String url, String[] urlParams) {
    StringBuilder urlBuilder = new StringBuilder(url);
    if (urlParams == null || urlParams.length == 0) {
      mOkCallBuilder.url(url);
    }
    else {
      if (!url.contains("?")) {
        urlBuilder.append("?");
      }

      for (int i = 0; i < urlParams.length; i += 2) {
        urlBuilder.append('&').append(urlParams[i])
            .append('=').append(urlParams[i + 1]);
      }
    }
    mOkCallBuilder.url(urlBuilder.toString());
    return this;
  }

  @Override public HttpUtils cache(int seconds) {
    mOkCallBuilder.cache(seconds);
    return this;
  }

  @Override public HttpUtils params(String json) {
    mOkCallBuilder.params(json);
    return this;
  }

  @Override public HttpUtils params(String[] params) {
    mOkCallBuilder.params(params);
    return this;
  }

  @Override public HttpUtils params(Object[] params) {
    mOkCallBuilder.params(params);
    return this;
  }

  @Override public HttpUtils params(JSONObject jsonParam) {
    mOkCallBuilder.params(jsonParam);
    return this;
  }

  @Override public HttpUtils header(String[] headers) {
    mOkCallBuilder.header(headers);
    return this;
  }

  @Override public HttpUtils json() {
    mOkCallBuilder.json();
    return this;
  }

  @Override public HttpUtils urlencoded() {
    mOkCallBuilder.urlencoded();
    return this;
  }

  @Override public HttpUtils multiPart() {
    mOkCallBuilder.multiPart();
    return this;
  }

  private static boolean isOk(int code) {
    return code >= 200 && code < 300;
  }

  @Override public Task download(File target) throws HttpException {
    Call call = mOkCallBuilder.get().build();
    final Task task = new Task(call);
    try {
      Response response = call.execute();
      task.setCode(response.code());
      if (isOk(task.code())) {
        ResponseBody body = response.body();
        if (body != null) {
          InputStream is = body.byteStream();
          final long totalLength = body.contentLength();
          FileOutputStream fos = null;
          //noinspection TryFinallyCanBeTryWithResources
          try {
            fos = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int l;
            long currentLength = 0;
            while ((l = is.read(buffer)) != -1) {
              fos.write(buffer, 0, l);
              currentLength += buffer.length;
              task.setProgress((int) (currentLength / totalLength * 100));
            }
            body.close();
            close(fos);
            task.setFile(target);
            task.setResult(target.getPath());
            return task;
          }
          catch (IOException e) {
            throw new HttpException(e);
          }
          finally {
            body.close();
            close(fos);
          }
        }
        else {
          throw new HttpException("body is null");
        }
      }
      else {
        throw new HttpException("code not 200: " + task.code());
      }
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
  }

  @Override public Task download(final File target, final DownloadListener listener) {
    Call call = mOkCallBuilder.get().build();
    final Task task = new Task(call);
    call.enqueue(new okhttp3.Callback() {
      @Override public void onFailure(@NonNull Call call, @NonNull final IOException e) {
        mHandler.post(new Runnable() {
          @Override public void run() {
            listener.onFailed(task, new HttpException(e));
          }
        });
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        task.setCode(response.code());
        if (isOk(task.code())) {
          final ResponseBody body = response.body();
          if (body != null) {
            mExec.execute(new Runnable() {
              @Override public void run() {
                InputStream is = body.byteStream();
                final long totalLength = body.contentLength();
                FileOutputStream fos = null;
                //noinspection TryFinallyCanBeTryWithResources
                try {
                  fos = new FileOutputStream(target);
                  byte[] buffer = new byte[1024];
                  int l;
                  long currentLength = 0;
                  while ((l = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, l);
                    currentLength += buffer.length;
                    task.setProgress((int) (currentLength * 1F / totalLength * 100));
                    mHandler.post(new Runnable() {
                      @Override public void run() {
                        listener.onProgress(task.progress());
                      }
                    });
                  }
                  fos.flush();
                  close(fos);
                  body.close();
                  task.setFile(target);
                  task.setResult(target.getPath());
                  mHandler.post(new Runnable() {
                    @Override public void run() {
                      listener.onSucceed(task);
                    }
                  });
                }
                catch (final IOException e) {
                  mHandler.post(new Runnable() {
                    @Override public void run() {
                      listener.onFailed(task, new HttpException(e));
                    }
                  });
                }
                finally {
                  close(fos);
                  body.close();
                }
              }
            });
          }
          else {
            mHandler.post(new Runnable() {
              @Override public void run() {
                listener.onFailed(task, new HttpException("body is null"));
              }
            });
          }
        }
        else {
          mHandler.post(new Runnable() {
            @Override public void run() {
              listener.onFailed(task, new HttpException("code not 200: " + task.code()));
            }
          });
        }
      }
    });
    return task;
  }

  @Override public Task get() throws HttpException {
    Call call = mOkCallBuilder.get().build();
    final Task task = new Task(call);
    try {
      Response response = call.execute();
      task.setCode(response.code());
      if (isOk(task.code())) {
        ResponseBody body = response.body();
        if (body != null) {
          task.setResult(body.string());
          body.close();
        }
        else {
          throw new HttpException("body is null");
        }
      }
      else {
        throw new HttpException("code not 200: " + task.code());
      }
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    return task;
  }

  @Override public Task get(final Listener listener) {
    Call call = mOkCallBuilder.get().build();
    final Task task = new Task(call);
    call.enqueue(new okhttp3.Callback() {
      @Override public void onFailure(@NonNull Call call, @NonNull final IOException e) {
        mHandler.post(new Runnable() {
          @Override public void run() {
            listener.onFailed(task, new HttpException(e));
          }
        });
      }

      @Override public void onResponse(@NonNull Call call, @NonNull Response response)
          throws IOException {
        final int code = response.code();
        task.setCode(code);
        if (isOk(code)) {
          ResponseBody body = response.body();
          if (body != null) {
            final String string = body.string();
            task.setResult(string);
            mHandler.post(new Runnable() {
              @Override public void run() {
                listener.onSucceed(task);
              }
            });
          }
          else {
            mHandler.post(new Runnable() {
              @Override public void run() {
                listener.onFailed(task, new HttpException("body is null"));
              }
            });
          }
        }
        else {
          mHandler.post(new Runnable() {
            @Override public void run() {
              listener.onFailed(task, new HttpException("code not 200: " + code));
            }
          });
        }
      }
    });
    return task;
  }

  @Override public Task post() throws HttpException {
    Call call = mOkCallBuilder.build();
    final Task task = new Task(call);
    try {
      Response response = call.execute();
      task.setCode(response.code());
      if (isOk(task.code())) {
        ResponseBody body = response.body();
        if (body != null) {
          task.setResult(body.string());
        }
        else {
          throw new HttpException("body is null");
        }
      }
      else {
        throw new HttpException("code not 200: " + task.code());
      }
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    return task;
  }

  @Override public Task post(final Listener listener) {
    Call call = mOkCallBuilder.build();
    final Task task = new Task(call);
    call.enqueue(new okhttp3.Callback() {
      @Override public void onFailure(@NonNull Call call, @NonNull final IOException e) {
        mHandler.post(new Runnable() {
          @Override public void run() {
            listener.onFailed(task, new HttpException(e));
          }
        });
      }

      @Override public void onResponse(@NonNull Call call, @NonNull Response response)
          throws IOException {
        final int code = response.code();
        task.setCode(code);
        if (isOk(code)) {
          ResponseBody body = response.body();
          if (body != null) {
            final String string = body.string();
            task.setResult(string);
            mHandler.post(new Runnable() {
              @Override public void run() {
                listener.onSucceed(task);
              }
            });
          }
          else {
            mHandler.post(new Runnable() {
              @Override public void run() {
                listener.onFailed(task, new HttpException("body is null"));
              }
            });
          }
        }
        else {
          mHandler.post(new Runnable() {
            @Override public void run() {
              listener.onFailed(task, new HttpException("code not 200: " + code));
            }
          });
        }
      }
    });
    return task;
  }

  private static void close(Closeable closeable) {
    if (closeable != null) {
      try { closeable.close(); }
      catch (IOException ignore) {}
    }
  }
}
