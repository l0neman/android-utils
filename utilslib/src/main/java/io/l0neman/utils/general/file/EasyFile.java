package io.l0neman.utils.general.file;

import java.io.File;
import java.io.IOException;

/**
 * Created by l0neman on 2019/05/30.
 * <p>
 * Easy file utils.
 */
public class EasyFile {

  /** Easy file utils exception. */
  public static class EasyFileException extends Exception {
    public EasyFileException(String message) {
      super(message);
    }

    public EasyFileException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Get the merged file name, automatic processing as follows:
   * <p>
   * base: aaa/ child: \bbb -> aaa + {@link File#separator} + bbb
   * <p>
   * base: aaa child: bbb   -> aaa + {@link File#separator} + bbb
   * <p>
   * base: aaa\ child: /bbb -> aaa + {@link File#separator} + bbb
   *
   * @param base  parent dir path.
   * @param child child file path.
   * @return merged file name.
   */
  public static String makeFileName(String base, String child) {
    if (base == null || base.length() == 0 || child == null || child.length() == 0) {
      throw new NullPointerException("invalid.");
    }

    final char baseLast = base.charAt(base.length() - 1);
    final char childFirst = child.charAt(0);
    // 处理可能冲突的路径符号。
    if (baseLast == '/' || baseLast == '\\') {
      if (childFirst == '/' || childFirst == '\\') {
        child = child.substring(1);
      }
    } else {
      if (childFirst != '/' && childFirst != '\\') {
        child = File.separator + child;
      }
    }

    // 统一路径符号。
    final String pre = base + child;
    return '/' == File.separatorChar ?
        pre.replace('\\', File.separatorChar) :
        pre.replace('/', File.separatorChar);
  }

  /**
   * Create a folder. If a folder with the same name already exists, it will not be processed.
   * In other cases, an exception occurs.
   *
   * @param dir target dir.
   * @return target dir.
   *
   * @throws EasyFileException other cases。
   */
  public static File createDir(File dir) throws EasyFileException {
    return checkAndCreateDir(requireNonNull(dir));
  }

  /**
   * Create a file. If the parent directory of the file does not exist, it will be created
   * automatically. If the file exists, it will not be processed. Otherwise, an exception will occur.
   *
   * @param file target file。
   * @return target file。
   *
   * @throws EasyFileException other cases。
   */
  public static File createFile(File file) throws EasyFileException {
    return checkAndCreateFile(requireNonNull(file));
  }

  /**
   * Delete the file. If the file does not exist, it will not be processed. If it is a directory,
   * it will be deleted.
   *
   * @param file target file or dir.
   * @return target file or dir.
   *
   * @throws EasyFileException unexpected exception.
   */
  public static File deleteFile(File file) throws EasyFileException {
    return checkAndRemove(requireNonNull(file));
  }

  /**
   * Same as {@link #deleteFile(File)}
   */
  public static File deleteDir(File dir) throws EasyFileException {
    return checkAndRemove(requireNonNull(dir));
  }

  // file utils:

  private static <T> T requireNonNull(T obj) {
    if (obj == null)
      throw new NullPointerException();
    return obj;
  }

  private static File checkAndRemove(File file) throws EasyFileException {
    if (!file.exists()) {
      return file;
    }

    if (file.isFile()) {
      try {
        //noinspection StatementWithEmptyBody
        if (file.delete()) {
          // Log.d(TAG, "remove file ok: " + dirPath);
        }
      } catch (RuntimeException e) {
        throw new EasyFileException("remove file error", e);
      }
    }

    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files == null || files.length == 0) {
        return file;
      }

      for (File fi : files) {
        checkAndRemove(fi);
      }

      try {
        //noinspection StatementWithEmptyBody
        if (file.delete()) {
          // Log.d(TAG, "remove file ok: " + dirPath);
        }
      } catch (RuntimeException e) {
        throw new EasyFileException("remove dir error", e);
      }
    }

    return file;
  }

  private static File checkAndCreateFile(File file) throws EasyFileException {
    if (file.exists()) {
      return file;
    }

    final File parentFile = file.getParentFile();
    if (parentFile != null && !parentFile.exists()) {
      checkAndCreateDir(parentFile);
    }

    try {
      //noinspection StatementWithEmptyBody
      if (file.createNewFile()) {
        // Log.i(TAG, "create file: " + file + " ok.");
      }
    } catch (IOException e) {
      // Log.e(TAG, "create file: " + file + " error", e);
      throw new EasyFileException("create file error", e);
    }
    return file;
  }

  private static File checkAndCreateDir(File dir) throws EasyFileException {

    if (dir.exists()) {
      return dir;
    }

    try {
      //noinspection StatementWithEmptyBody
      if (dir.mkdirs()) {
        // Log.d(TAG, "create dir ok: " + dirPath);
      }
    } catch (RuntimeException e) {
      throw new EasyFileException("create dir error", e);
    }

    return dir;
  }
}

