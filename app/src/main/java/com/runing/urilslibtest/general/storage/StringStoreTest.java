package com.runing.urilslibtest.general.storage;

import android.content.Context;
import android.util.Log;

import com.runing.utilslib.general.storage.IStore;
import com.runing.utilslib.general.storage.IStringStore;
import com.runing.utilslib.general.storage.StringStore;

import java.io.File;

public class StringStoreTest {

  private void test(Context context) {

    // 默认路径 "${fileDir}/ss/"。
    IStringStore stringStore1 = new StringStore(context);
    // 或指定自定义路径。
    IStringStore stringStore2 = new StringStore(new File(context.getFilesDir(), "my").getPath());

    // 同步存储。
    stringStore1.open("file1").write("content.");
    // 异步存储。
    stringStore1.open("file2").writeAsync("content.");


    // 同步读取。
    final String file1Content = stringStore1.open("file1").read();
    // 异步读取。
    stringStore1.open("file2").readAsync(new IStore.ReadCallback<String>() {
      @Override public void onValue(String value) {
        Log.d("file2Content", value);
      }
    });

    // 删除单个文件。
    stringStore1.delete("file1");
    // 清空所有文件。
    stringStore2.deleteAll();

  }
}
