package com.runing.urilslibtest.general.storage;

import android.content.Context;
import android.util.Log;

import com.runing.utilslib.general.storage.StringStore;

public class StringStoreTest {

  private void test(Context context) {
    // 初始化路径，放在 application 中。
    StringStore.init(context);
    // 同步存储。
    StringStore.open("file1").write("content");
    // 异步存储。
    StringStore.open("file2").writeAsync("content");

    // 同步读取。
    final String file1Content = StringStore.read("file1");
    // 异步读取。
    StringStore.readAsync("file2", new StringStore.ReadCallback() {
      @Override public void onRead(String content) {
        Log.d("file2Content", content);
      }
    });
  }
}
