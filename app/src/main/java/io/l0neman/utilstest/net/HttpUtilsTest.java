package io.l0neman.utilstest.net;

import io.l0neman.utils.net.HttpUtils;
import io.l0neman.utils.net.OkHttpUtils;

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
