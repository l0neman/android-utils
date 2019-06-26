package io.l0neman.utilstest.general.file;

import java.io.File;

import io.l0neman.utils.general.file.EasyFile;

public class EasyFileTest {

  public void test() {
    // 将不规则的目录后缀和文件名前缀组合成正常路径名。
    // => Linux:    myDir/file
    // => Windows:  myDir\\file
    EasyFile.makeFileName("myDir/", "file");
    EasyFile.makeFileName("myDir\\", "/file");
    EasyFile.makeFileName("myDir/", "\\file");
    EasyFile.makeFileName("myDir", "file");

    // 创建目录或文件。
    try {
      EasyFile.createDir(new File("myDir"));
    } catch (EasyFile.EasyFileException e) {
      // 创建失败。
    }

    try {
      EasyFile.createFile(new File("myFile"));
    } catch (EasyFile.EasyFileException e) {
      // 创建失败。
    }

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
  }
}
