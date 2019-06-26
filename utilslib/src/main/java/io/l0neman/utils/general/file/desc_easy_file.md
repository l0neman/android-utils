# EasyFile

[源码 - EasyFile.java](EasyFile.java)

简单文件工具，简化平时最常用的文件功能，一行即可，无需考虑太多。

（类并不一定要复杂，重要的是好用😊）。

## 处理文件名

```java
// 将不规则的目录后缀和文件名前缀组合成正常路径名。
// => Linux:    myDir/file
// => Windows:  myDir\\file
EasyFile.makeFileName("myDir/", "file");
EasyFile.makeFileName("myDir\\", "/file");
EasyFile.makeFileName("myDir/", "\\file");
EasyFile.makeFileName("myDir", "file");
```

## 创建目录或文件

```java
// 创建目录。
try {
  EasyFile.createDir(new File("myDir"));
} catch (EasyFile.EasyFileException e) {
  // 创建失败。
}

// 创建文件。
try {
  EasyFile.createFile(new File("myFile"));
} catch (EasyFile.EasyFileException e) {
  // 创建失败。
}
```

## 删除目录或文件

```java
// 递归删除任意目录。
try {
  EasyFile.deleteDir(new File("myDir"));
} catch (EasyFile.EasyFileException ignore) {
}

// 删除任意文件。
try {
  EasyFile.deleteDir(new File("myFile"));
} catch (EasyFile.EasyFileException ignore) {
}
```