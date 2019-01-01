package com.runing.urilslibtest.net;

import com.runing.utilslib.net.HttpUtils;
import com.runing.utilslib.net.OkHttpUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class HttpUtilsTest {

  private HttpUtils httpUtils = new OkHttpUtils.Builder()
      .cookieJar(new CookieJar() {
        @Override public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {}

        @Override public List<Cookie> loadForRequest(HttpUrl url) { return null; }
      })
      .connTimeOut(10, TimeUnit.SECONDS)
      .readTimeOut(10, TimeUnit.SECONDS)
      .cacheDir(new File("cache"))
      .cacheSize(50 * 1024)
      .build();

  public void test() {

  }
}
