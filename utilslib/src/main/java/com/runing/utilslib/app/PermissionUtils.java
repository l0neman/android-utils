package com.runing.utilslib.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Permission request utils
 * Created by runing on 2016/10/12.
 */

public final class PermissionUtils {

  /* Permission request code */
  private static final int PERMISSION_REQUEST_CODE = Short.MAX_VALUE;

  private List<String> mRequestPermissions = new ArrayList<>();

  private final SinglePermissionCallback mCallBack;

  private PermissionUtils(@NonNull SinglePermissionCallback callBack) {
    this.mCallBack = callBack;
  }

  public static PermissionUtils newInstance(@NonNull MultiPermissionCallBack callBack) {
    return new PermissionUtils(callBack);
  }

  public static PermissionUtils newInstance(@NonNull SinglePermissionCallback callback) {
    return new PermissionUtils(callback);
  }

  public interface SinglePermissionCallback {
    void onGrantedPermission(String permission);

    void onDeniedPermission(String permission);

    void onRationalePermission(String permission);
  }

  public interface MultiPermissionCallBack extends PermissionUtils.SinglePermissionCallback {
    void onGrantedAll();

    void onDeniedPart();
  }

  public static class SinglePermissionAdapter implements SinglePermissionCallback {

    @Override public void onGrantedPermission(String permission) {}

    @Override public void onDeniedPermission(String permission) {}

    @Override public void onRationalePermission(String permission) {}
  }

  public static class MultiPermissionAdapter implements MultiPermissionCallBack {

    @Override public void onGrantedPermission(String permission) { }

    @Override public void onDeniedPermission(String permission) { }

    @Override public void onRationalePermission(String permission) { }

    @Override public void onGrantedAll() { }

    @Override public void onDeniedPart() { }
  }

  /**
   * @param ignoreRationale 是否忽略权限解释
   */
  public void checkAndRequestPermission(Activity activity, boolean ignoreRationale,
                                        @NonNull String permission) {
    mRequestPermissions.clear();

    if (mCallBack instanceof MultiPermissionCallBack) {
      checkAndRequestMultiPermissions(activity, ignoreRationale, new String[]{permission});
    } else {
      checkAndRequestSinglePermission(activity, ignoreRationale, permission);
    }
  }

  /**
   * like {@link #checkAndRequestPermission}
   */
  public void checkAndRequestPermissions(Activity activity, boolean ignoreRationale,
                                         @NonNull String... permissions) {
    if (permissions.length == 0) {
      throw new AssertionError("no permissions");
    }

    mRequestPermissions.clear();

    if (mCallBack instanceof MultiPermissionCallBack) {
      checkAndRequestMultiPermissions(activity, ignoreRationale, permissions);
    } else {
      checkAndRequestSinglePermission(activity, ignoreRationale, permissions[0]);
    }
  }

  private void checkAndRequestSinglePermission(Activity activity, boolean ignoreRationale,
                                               String permission) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
      if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        mCallBack.onRationalePermission(permission);
        return;
      }
      mRequestPermissions.add(permission);
    } else {
      mCallBack.onGrantedPermission(permission);
    }
    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, new String[]{mRequestPermissions.get(0)}, PERMISSION_REQUEST_CODE
      );
    }
  }

  private void checkAndRequestMultiPermissions(Activity activity, boolean ignoreRationale,
                                               String[] permissions) {
    for (final String permission : permissions) {
      if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
        if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
          mCallBack.onRationalePermission(permission);
          return;
        }
        mRequestPermissions.add(permission);
      } else {
        mCallBack.onGrantedPermission(permission);
      }
    }
    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, mRequestPermissions.toArray(new String[mRequestPermissions.size()]), PERMISSION_REQUEST_CODE
      );
    }
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_CODE) {
      return;
    }
    if (grantResults.length == 0 || permissions.length == 0 || grantResults.length != permissions.length) {
      return;
    }
    if (mCallBack instanceof MultiPermissionCallBack) {
      handleMultiPermissionResult(permissions, grantResults);
    } else {
      handleSinglePermissionResult(permissions[0], grantResults[0]);
    }
  }

  private void handleMultiPermissionResult(String[] permissions, int[] grantResults) {
    boolean isAllGranted = true;
    final MultiPermissionCallBack callBack = (MultiPermissionCallBack)mCallBack;
    for (int i = 0; i < permissions.length; i++) {
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        callBack.onGrantedPermission(permissions[i]);
      } else {
        isAllGranted = false;
        callBack.onDeniedPermission(permissions[i]);
      }
    }
    if (isAllGranted) {
      callBack.onGrantedAll();
    } else {
      callBack.onDeniedPart();
    }
  }

  private void handleSinglePermissionResult(String permission, int grantResults) {
    if (grantResults == PackageManager.PERMISSION_GRANTED) {
      mCallBack.onGrantedPermission(permission);
    } else {
      mCallBack.onDeniedPermission(permission);
    }
  }
}
