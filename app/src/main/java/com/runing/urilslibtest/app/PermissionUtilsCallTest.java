package com.runing.urilslibtest.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.widget.Toast;

import com.runing.urilslibtest.R;
import com.runing.utilslib.app.PermissionUtils;

public class PermissionUtilsCallTest extends Activity {

  private PermissionUtilsCallTest self() { return this; }

  private PermissionUtils getmPermissionUtils = PermissionUtils.newInstance(
      new PermissionUtils.MultiPermissionAdapter() {
        @Override public void onGrantedPermission(String permission) {
          /* 每个通过的权限都会从这里回调 */
        }

        @Override public void onDeniedPermission(String permission) {
          /* 每个被拒绝的权限都会从这里回调 */
        }

        @Override public void onRationalePermission(String permission) {
          /* 每个需要被解释的权限都会从这里回调 */
        }

        @Override public void onGrantedAll() {
          /* 全部权限通过 */
        }

        @Override public void onDeniedPart() {
          /* 部分权限或者全部权限没通过 */
        }
      });

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  private PermissionUtils mPermissionUtils = PermissionUtils.newInstance(
      new PermissionUtils.SinglePermissionAdapter() {
        @Override public void onGrantedPermission(String permission) {
          /* 权限通过 */
          Toast.makeText(self(), "granted permissions.", Toast.LENGTH_SHORT).show();
        }

        @Override public void onDeniedPermission(String permission) {
          /* 权限被拒绝 */
          Toast.makeText(self(), "denied permissions.", Toast.LENGTH_SHORT).show();
        }

        @Override public void onRationalePermission(String permission) {
          Toast.makeText(self(), "we need permissions.", Toast.LENGTH_SHORT).show();
          /* 解释权限 */
          new AlertDialog.Builder(PermissionUtilsCallTest.this)
              .setPositiveButton("again", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                  /* 再次请求 */
                  mPermissionUtils.checkAndRequestPermissions(self(),
                      true, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
              })
              .setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                  Toast.makeText(self(), "no", Toast.LENGTH_SHORT).show();
                }
              })
              .create()
              .show();
        }
      }
  );

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      mPermissionUtils.checkAndRequestPermission(self(), false,
          Manifest.permission.READ_EXTERNAL_STORAGE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      mPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }
}
