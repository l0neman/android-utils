package io.l0neman.utils.app;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission request utils
 * Created by runing on 2016/10/12.
 */

public final class Permission {

  private List<String> mRequestPermissions = new ArrayList<>();
  private Set<String> mGrantedPermissions = new HashSet<>();

  private final SingleCallback mCallBack;

  private Permission(@androidx.annotation.NonNull SingleCallback callBack) {
    this.mCallBack = callBack;
  }

  public static Permission newInstance(@androidx.annotation.NonNull MultiCallBack callBack) {
    return new Permission(callBack);
  }

  public static Permission newInstance(@androidx.annotation.NonNull SingleCallback callback) {
    return new Permission(callback);
  }

  public interface SingleCallback {
    void onGranted(int requestCode, String permission);

    void onDenied(int requestCode, String permission);

    void onRationale(int requestCode, String permission);
  }

  public interface MultiCallBack extends SingleCallback {
    void onGrantedAll(int requestCode);

    void onGrantedPart(int requestCode, Set<String> grantedPermissions);
  }

  public static class SingleAdapter implements SingleCallback {

    @Override public void onGranted(int requestCode, String permission) {}

    @Override public void onDenied(int requestCode, String permission) {}

    @Override public void onRationale(int requestCode, String permission) {}
  }

  public static class MultiAdapter implements MultiCallBack {

    @Override public void onGranted(int requestCode, String permission) {}

    @Override public void onDenied(int requestCode, String permission) {}

    @Override public void onRationale(int requestCode, String permission) {}

    @Override public void onGrantedAll(int requestCode) {}

    @Override public void onGrantedPart(int requestCode, Set<String> grantedPermissions) {}
  }

  /**
   * @param ignoreRationale 是否忽略权限解释
   */
  public void checkAndRequest(Activity activity, int requestCode, boolean ignoreRationale,
                              @androidx.annotation.NonNull String permission) {
    mRequestPermissions.clear();
    mGrantedPermissions.clear();

    if (mCallBack instanceof MultiCallBack) {
      checkAndRequestMulti(activity, requestCode, ignoreRationale, new String[]{permission});
    } else {
      checkAndRequestSingle(activity, requestCode, ignoreRationale, permission);
    }
  }

  /**
   * like {@link #checkAndRequest}
   */
  public void checkAndRequest(Activity activity, int requestCode, boolean ignoreRationale,
                              @androidx.annotation.NonNull String... permissions) {
    if (permissions.length == 0) {
      throw new AssertionError("no permissions");
    }

    mRequestPermissions.clear();
    mGrantedPermissions.clear();

    if (mCallBack instanceof MultiCallBack) {
      checkAndRequestMulti(activity, requestCode, ignoreRationale, permissions);
    } else {
      checkAndRequestSingle(activity, requestCode, ignoreRationale, permissions[0]);
    }
  }

  private void checkAndRequestSingle(Activity activity, int requestCode, boolean ignoreRationale,
                                     String permission) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
      if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        mCallBack.onRationale(requestCode, permission);
        return;
      }

      mRequestPermissions.add(permission);
    } else {
      mCallBack.onGranted(requestCode, permission);
    }

    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, new String[]{mRequestPermissions.get(0)}, requestCode
      );
    }
  }

  private void checkAndRequestMulti(Activity activity, int requestCode, boolean ignoreRationale,
                                    String[] permissions) {
    for (final String permission : permissions) {
      if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
        if (!ignoreRationale && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
          mCallBack.onRationale(requestCode, permission);
          mGrantedPermissions.add(permission);
          return;
        }

        mRequestPermissions.add(permission);
      } else {
        mCallBack.onGranted(requestCode, permission);
      }
    }

    if (!mRequestPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          activity, mRequestPermissions.toArray(new String[0]), requestCode
      );
    } else {
      ((MultiCallBack) mCallBack).onGrantedAll(requestCode);
    }
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (permissions.length == 0 || grantResults.length != permissions.length) {
      return;
    }

    if (mCallBack instanceof MultiCallBack) {
      handleMultiPermissionResult(requestCode, permissions, grantResults);
    } else {
      handleSinglePermissionResult(requestCode, permissions[0], grantResults[0]);
    }
  }

  private void handleMultiPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
    boolean isAllGranted = true;
    final MultiCallBack callBack = (MultiCallBack) mCallBack;
    for (int i = 0; i < permissions.length; i++) {
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        callBack.onGranted(requestCode, permissions[i]);
      } else {
        isAllGranted = false;
        callBack.onDenied(requestCode, permissions[i]);
      }
    }

    if (isAllGranted) {
      callBack.onGrantedAll(requestCode);
    } else {
      callBack.onGrantedPart(requestCode, mGrantedPermissions);
    }
  }

  private void handleSinglePermissionResult(int requestCode, String permission, int grantResults) {
    if (grantResults == PackageManager.PERMISSION_GRANTED) {
      mCallBack.onGranted(requestCode, permission);
      mGrantedPermissions.add(permission);
    } else {
      mCallBack.onDenied(requestCode, permission);
    }
  }
}
