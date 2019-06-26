# Closer

[源码 - Closer.java](Closer.java)

关闭 IO 流的工具，参考了 guava 库的用法。

内部使用栈保存流的引用，关闭时遵循先进后出，最多保存 10 个。

## 用法

```java
Closer closer = Closer.create();
try {
  InputStream is = closer.register(
      new FileInputStream("input"));

  OutputStream os = closer.register(
      new FileOutputStream("output"));

  byte[] buffer = new byte[1024];
  while ((is.read(buffer)) != 0) {
    os.write(buffer);
  }
} catch (IOException e) {
  e.printStackTrace();
} finally {
  // 忽略 IOException；
  // 如果有 RuntimeException，将抛出第一个异常。
  closer.close();
}
```