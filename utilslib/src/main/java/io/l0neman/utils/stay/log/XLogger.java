package io.l0neman.utils.stay.log;

import android.util.Log;

/**
 * Created by l0neman on 2019/06/29.
 */
public class XLogger {
  private static final String MAIN_TAG = "X";

  // 是否开启附加栈信息。
  private static final boolean STACK_PRINT = true;

  private static final boolean PRINT = true;

  // 打印栈的层数。
  private static final int STACK_COUNT = 1;
  // 栈寻找的起始深度。
  private static final int STACK_DEPTH = 1;
  // 栈偏移，基于标准日志方法的偏移。
  private static final int STACK_OFFSET = 4;

  private static String getThreadName() {
    return Thread.currentThread().getName();
  }

  private static String getLog(String log) {
    String stackInfo = STACK_PRINT ? stackInfo() : "";
    return log + " [" + getThreadName() + ']' + stackInfo;
  }

  private static String getTag(String tag) {
    return MAIN_TAG + "#" + tag;
  }

  private static String stackInfo() {
    final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    final int stackDepth = getStackDepth(elements);
    int bottomStackDepth = stackDepth + STACK_OFFSET;
    int topStackDepth = stackDepth + STACK_COUNT + STACK_OFFSET;
    if (bottomStackDepth >= elements.length) {
      bottomStackDepth = elements.length - 1;
    }
    if (topStackDepth > elements.length) {
      topStackDepth = elements.length;
    }

    StringBuilder builder = new StringBuilder();
    StringBuilder stackTable = new StringBuilder();
    for (int i = topStackDepth - 1; i >= bottomStackDepth; i--) {
      final String formatStackInfo = getFormatStackInfo(elements[i]);
      builder.append(stackTable).append(formatStackInfo).append('\n');
      stackTable.append("  ");
    }
    return builder.toString();
  }

  private static String getFormatStackInfo(StackTraceElement element) {
    return "(" + element.getFileName() + ':' + element.getLineNumber() + ')';
  }

  private static int getStackDepth(StackTraceElement[] elements) {
    for (int i = STACK_DEPTH; i < elements.length; i++) {
      final StackTraceElement element = elements[i];
      if (!element.getClassName().split("\\$")[0].equals(XLogger.class.getName())) {
        return i;
      }
    }

    return -1;
  }

  public static void i(String tag, String format, Object... args) {
    if (PRINT) {
      Log.i(getTag(tag), getLog(String.format(format, args)));
    }
  }

  public static void i(String tag, Throwable tr, String format, Object... args) {
    if (PRINT) {
      Log.i(getTag(tag), getLog(String.format(format, args)), tr);
    }
  }

  public static void d(String tag, String format, Object... args) {
    if (PRINT) {
      Log.d(getTag(tag), getLog(String.format(format, args)));
    }
  }

  public static void d(String tag, Throwable tr, String format, Object... args) {
    if (PRINT) {
      Log.d(getTag(tag), getLog(String.format(format, args)), tr);
    }
  }

  public static void dL(String tag, String format, Object... args) {
    if (!PRINT) {
      return;
    }

    String log = String.format(format, args);

    final int k = 3000;
    if (log.length() < k) {
      Log.d(getTag(tag), log);
      return;
    }

    synchronized (XLogger.class) {
      int i = 0;
      int lI = 0;
      int length = log.length();
      do {
        i += k;
        if (i >= length) {
          i = length - 1;
        }

        Log.d(getTag(tag), log.substring(lI, i));
        lI = i + 1;
      } while (i != length - 1);
    }
  }

  public static void w(String tag, String format, Object... args) {
    if (PRINT) {
      Log.w(getTag(tag), getLog(String.format(format, args)));
    }
  }

  public static void w(String tag, Throwable tr, String format, Object... args) {
    if (PRINT) {
      Log.w(getTag(tag), getLog(String.format(format, args)), tr);
    }
  }

  public static void w(String tag, Throwable tr) {
    if (PRINT) {
      Log.w(getTag(tag), tr);
    }
  }

  public static void e(String tag,String format, Object... args) {
    if (PRINT) {
      Log.e(getTag(tag), getLog(String.format(format, args)));
    }
  }

  public static void e(String tag, Throwable tr, String format, Object... args) {
    if (PRINT) {
      Log.e(getTag(tag), getLog(String.format(format, args)), tr);
    }
  }

}
