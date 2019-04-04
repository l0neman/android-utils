# StringStore

[源码 - StringStore.java](./StringStore.java)

简单字符串存储工具，写入读取单个文件。
（线程读写锁，异步保存后也可立即调用读取）。

## 1. 初始化

```java
// 初始化路径，在使用前调用。
StringStore.init(context);
// 或直接指定目录路径。
StringStore.init(new File(context.getFilesDir(), "my").getPath());
```

## 2. 使用

- 存储字符串至文件

```java
// 同步存储。
StringStore.open("file1").write("content");
// 异步存储。
StringStore.open("file2").writeAsync("content");
```

- 从文件读取文件

```java
// 同步读取。
final String file1Content = StringStore.read("file1");
// 异步读取。
StringStore.readAsync("file2", new StringStore.ReadCallback() {
  @Override public void onRead(String content) {
    Log.d("file2Content", content);
  }
});
```

- 删除文件

```java
// 异步删除单个文件。
StringStore.delete("file1");
// 异步清空所有文件。
StringStore.deleteAll();
```