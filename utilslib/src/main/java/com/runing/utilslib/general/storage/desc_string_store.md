# StringStore

[源码 - StringStore.java](./StringStore.java)

简单字符串存储工具，写入读取指定目录下的单个文件。
（线程读写锁，异步保存后也可立即调用读取）。

## 1. 初始化

```java
 // 默认目录 "${fileDir}/ss/"。
IStringStore stringStore1 = new StringStore(context);
// 或指定自定义路径。
IStringStore stringStore2 = new StringStore(new File(context.getFilesDir(), "my").getPath());
```

## 2. 使用

- 存储字符串至文件

```java
// 同步存储。
stringStore1.open("file1").write("content.");
// 异步存储。
stringStore1.open("file2").writeAsync("content.");
```

- 从文件读取文件

```java
// 同步读取。
final String file1Content = stringStore1.open("file1").read();
// 异步读取。
stringStore1.open("file2").readAsync(new IStore.ReadCallback<String>() {
  @Override public void onValue(String value) {
    Log.d("file2Content", value);
  }
});
```

- 删除文件

```java
// 删除单个文件。
stringStore1.delete("file1");
// 清空所有文件。
stringStore2.deleteAll();
```