package com.runing.urilslibtest.net;

import com.runing.utilslib.net.HttpUtils;
import com.runing.utilslib.net.OkHttpUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class HttpUtilsTest {

  private HttpUtils httpUtils = new OkHttpUtils.Builder()
      .connTimeOut(10, TimeUnit.SECONDS)
      .readTimeOut(10, TimeUnit.SECONDS)
      .cacheDir(new File("cache"))
      .cacheSize(50 * 1024)
      .build();


  public void test() {
  }
}
