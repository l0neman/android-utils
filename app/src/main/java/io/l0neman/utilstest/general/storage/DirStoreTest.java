package io.l0neman.utilstest.general.storage;

import android.content.Context;

import io.l0neman.utils.general.storage.DirStore;
import io.l0neman.utils.general.storage.IDirStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by l0neman on 2019/04/24.
 */
public class DirStoreTest {

  private void test(Context context) {
    // 1. 默认路径 "${fileDir}/ds/"。
    IDirStore dirStore = new DirStore(context);
    // 2. 或指定自定义路径。
    // IDirStore dirStore = new DirStore(new File(context.getFilesDir(), "my").getPath());

    // 下面的文件均在 DirStore 设置的路径下。

    // 同步存储在 test 文件下。
    try {
      dirStore.with("test", DirStore.STRING_IO_ADAPTER).write("content");
    } catch (IOException e) {
      // io error.
    }

    // 异步存储在 test 文件下。
    dirStore.with("test", DirStore.STRING_IO_ADAPTER).writeAsync("content1",
        new IDirStore.WriteCallback() { // callback 可为 null。
          @Override public void onError(IOException e) {
            // io error.
          }
        });

    // 同步读取 test 文件中的内容。
    try {
      final String content = dirStore.with("test", DirStore.STRING_IO_ADAPTER).read();
    } catch (IOException e) {
      // io error.
    }

    // 异步读取 test 文件中的内容。
    dirStore.with("test", DirStore.STRING_IO_ADAPTER).readAsync(new IDirStore.ReadCallback<String>() {
      @Override public void onValue(String value) {
        // read value.
      }

      @Override public void onError(IOException e) {
        // io error.
      }
    });

    // 设置存储适配器，保存自定义类型。（实例 JSONObject）
    final IDirStore.FileStore<JSONObject> custom = dirStore.with("custom",
        new IDirStore.FileIOAdapter<JSONObject>() {
          @Override public Class<JSONObject> typeToken() {
            return JSONObject.class;
          }

          @Override public void write(String file, JSONObject value) throws IOException {
            FileWriter writer = null;
            try {
              writer = new FileWriter(file);
              writer.write(value.toString());
            } finally {
              if (writer != null) {
                try { writer.close(); } catch (IOException ignore) {}
              }
            }
          }

          @Override public JSONObject read(String file) throws IOException {
            BufferedReader br = null;
            try {
              StringBuilder builder = new StringBuilder();
              br = new BufferedReader(new FileReader(file));
              String line;
              while ((line = br.readLine()) != null) {
                builder.append(line);
              }

              try {
                return new JSONObject(builder.toString());
              } catch (JSONException e) {
                return null;
              }
            } finally {
              if (br != null) {
                try { br.close(); } catch (IOException ignore) {}
              }
            }
          }
        });
    try {
      custom.write(new JSONObject());
    } catch (IOException ignore) {}
  }
}