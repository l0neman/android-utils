# DirStore

[源码 - IDirStore.java](IDirStore.java)
[源码 - DirStore.java](DirStore.java)

简单存储工具，写入读取指定目录下的文件。

1. 使用了线程锁控制读写，读取顺序和操作顺序保持一致。
2. 打开文件的实例使用 ThreadLocal 缓存，避免垃圾对象创建。

## 1. 创建实例

```java
// 1. 默认路径 "${fileDir}/ds/"。
IDirStore dirStore = new DirStore(context);
// 2. 或指定自定义路径。
// IDirStore dirStore = new DirStore(new File(context.getFilesDir(), "my").getPath());

// 使用 dirStore 打开（with 操作）的文件均在 dirStore 设置的路径下。
```

## 2. 使用

- 存储文件

```java
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
```

- 读取文件

```java
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
```

- 自定义存储数据类型

```java
// 设置存储适配器，保存自定义类型。（示例 JSONObject）
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
```

- 删除文件

```java
// 删除单个文件。
stringStore1.delete("file1");
// 清空所有文件。
stringStore2.deleteAll();
```