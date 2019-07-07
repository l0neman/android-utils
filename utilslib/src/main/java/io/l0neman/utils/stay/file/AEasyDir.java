package io.l0neman.utils.stay.reflect.file;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;

import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * Created by l0neman on 2019/05/30.
 */
public class AEasyDir {

  public static class AEasyDirException extends Exception {
    public AEasyDirException(String message) {
      super(message);
    }

    public AEasyDirException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static File getSdCardRoot(Context context) throws AEasyDirException {
    checkSdCardValid(context);
    return Environment.getExternalStorageDirectory();
  }

  public static File getCacheDir(Context context) {
    return context.getCacheDir();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static File getCodeCacheDir(Context context) {
    return context.getCodeCacheDir();
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public static File getDataDir(Context context) {
    return context.getDataDir();
  }

  public static File getDir(Context context, String dir) {
    return context.getDir(dir, Context.MODE_PRIVATE);
  }

  public static File getFilesDir(Context context) {
    return context.getFilesDir();
  }

  public static File getObbDir(Context context) throws AEasyDirException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      checkSdCardValid(context);
    }

    return context.getObbDir();
  }

  // utils:

  private static void checkSdCardValid(Context context) throws AEasyDirException {
    final boolean hasPermission = context.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;

    if (!hasPermission) {
      throw new AEasyDirException("write sd Permission denied");
    }

    if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      throw new AEasyDirException("media state error.");
    }
  }

}
