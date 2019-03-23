# StringStore

[源码 - StringStore.java](./StringStore.java)

简单字符串存储工具，写入读取单个文件。
（使用了 BIO；写入同一个文件时会阻塞其他写入和读取线程，读取时可并发）。

- 1. 初始化

```java
// 初始化路径，放在 application 中。
StringStore.init(context);
```

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