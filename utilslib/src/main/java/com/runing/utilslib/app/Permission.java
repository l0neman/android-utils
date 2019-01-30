package com.runing.utilslib.app;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Permission request utils
 * Created by runing on 2016/10/12.
 */

public final class Permission {

  /* Permission request code */
  private static final int PERMISSION_REQUEST_CODE = Short.MAX_VALUE;

  private List<String> mRequestPermissions = new ArrayList<>();
  private Set<String> mGrantedPermissions = new HashSet<>();

  private final SingleCallback mCallBack;

  private Permission(@NonNull SingleCallback callBack) {
    this.mCallBack = callBack;
  }

  public static Permission newInstance(@NonNull MultiCallBack callBack) {
    return new Permission(callBack);
  }

  public static Permission newInstance(@NonNull SingleCallback callback) {
    return new Permission(callback);
  }

  public interface SingleCallback {
    void onGranted(String permission);

    void onDenied(String permission);

    void onRationale(String permission);
  }

  public interface MultiCallBack extends SingleCallback {
    void onGrantedAll();

    void onGrantedPart(Set<String> grantedPermissions);
  }

  public static class SingleAdapter implements SingleCallback {

    @Override public void onGranted(String permission) {}

    @Override public void onDenied(String permission) {}

    @Override public void onRationale(String permission) {}
  }

  public static class MultiAdapter implements MultiCallBack {

    @Override public void onGranted(String permission) {}

    @Override public void onDenied(String permission) {}

    @Override public void onRationale(String permission) {}

    @Override public void onGrantedAll() {}

    @Override public void onGrantedPart(Set<String> grantedPermissions) {}
  }

  /**
   * @param ignoreRationale 是否忽略权限解释
   */
  public void checkAndRequest(Activity activity, boolean ignoreRationale,
                              @NonNull String permission) {
    mRequestPermissions.clear();
    mGrantedPermissions.clear();

    if (mCallBack instanceof MultiCallBack) {
      checkAndRequestMulti(activity, ignoreRationale, new String[]{permission});
    }
    else {
      checkAndRequestSingle(activity, ignoreRationale, permission);
    }
  }

  /**
   * like {@link #checkAndRequest}
   */
  public void checkAndRequest(Activity activity, boolean ignoreRationale,
                              @NonNull String... permissions) {
    if (permissions.length == 0) {
      throw new AssertionError("no permissions");
    }

    mRequestPermissions.clear();
    mGrantedPermissions.clear();

    if (mCallBack instanceof MultiCallBack) {
      checkAndRequestMulti(activity, ignoreRationale, permissions);
    }
    else {
      checkAndRequestSingle(activity, ignoreRationale, permissions[0]);
    }
  }

  private void checkAndRequestSingle(Activity activity, boolean ignoreRationale,
                                     String permission) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
      if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        mCallBack.onRationale(permission);
        return;
      }
      mRequestPermissions.add(permission);
    }
    else {
      mCallBack.onGranted(permission);
    }
    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, new String[]{mRequestPermissions.get(0)}, PERMISSION_REQUEST_CODE
      );
    }
  }

  private void checkAndRequestMulti(Activity activity, boolean ignoreRationale,
                                    String[] permissions) {
    for (final String permission : permissions) {
      if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
        if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
          mCallBack.onRationale(permission);
          mGrantedPermissions.add(permission);
          return;
        }
        mRequestPermissions.add(permission);
      }
      else {
        mCallBack.onGranted(permission);
      }
    }
    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, mRequestPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE
      );
    }
    else {
      ((MultiCallBack) mCallBack).onGrantedAll();
    }
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_CODE) {
      return;
    }
    if (grantResults.length == 0 || permissions.length == 0 || grantResults.length != permissions.length) {
      return;
    }
    if (mCallBack instanceof MultiCallBack) {
      handleMultiPermissionResult(permissions, grantResults);
    }
    else {
      handleSinglePermissionResult(permissions[0], grantResults[0]);
    }
  }

  private void handleMultiPermissionResult(String[] permissions, int[] grantResults) {
    boolean isAllGranted = true;
    final MultiCallBack callBack = (MultiCallBack) mCallBack;
    for (int i = 0; i < permissions.length; i++) {
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        callBack.onGranted(permissions[i]);
      }
      else {
        isAllGranted = false;
        callBack.onDenied(permissions[i]);
      }
    }
    if (isAllGranted) {
      callBack.onGrantedAll();
    }
    else {
      callBack.onGrantedPart(mGrantedPermissions);
    }
  }

  private void handleSinglePermissionResult(String permission, int grantResults) {
    if (grantResults == PackageManager.PERMISSION_GRANTED) {
      mCallBack.onGranted(permission);
      mGrantedPermissions.add(permission);
    }
    else {
      mCallBack.onDenied(permission);
    }
  }
}
