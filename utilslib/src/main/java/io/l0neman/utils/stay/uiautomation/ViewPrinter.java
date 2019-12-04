package io.l0neman.utils.stay.uiautomation;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

import io.virtualapp.support.VaLogger;
import io.virtualapp.utils.ReflectV;

/**
 * Created by l0neman on 2019/11/26.
 */
public class ViewPrinter {

  private static final String TAG = ViewPrinter.class.getSimpleName();

  public static void printViews(List<View> views) {
    for (View view : views) {
      printView(view);
    }
  }

  public static void printViews(Object fragment) {
    View mView = ReflectV.with(fragment).injector().field("mView").getQuietly();
    printView(mView);
  }

  public static void printViews(Activity activity, XUiAutomation automation) {
    Class<?> fac;
    try {
      fac = activity.getClassLoader().loadClass("android.support.v4.app.FragmentActivity");
    } catch (Throwable e) {
      fac = null;
    }

    if ((fac != null && fac.isInstance(activity)) || activity instanceof FragmentActivity) {
      Object sfm = ReflectV.with(activity).invoker().method("getSupportFragmentManager").invokeQuietly();

      new Thread(new Runnable() {
        @Override public void run() {
          List<Object> fragments = ReflectV.with(sfm).invoker().method("getFragments").invokeQuietly();
          VaLogger.i(TAG, ">> print fragments: " + fragments.size());

          while (fragments.isEmpty()) {
            SystemClock.sleep(1000);
            fragments = ReflectV.with(sfm).invoker().method("getFragments").invokeQuietly();
            VaLogger.i(TAG, ">> print fragments: " + fragments.size());
          }

          final View decorView = activity.getWindow().getDecorView();
          decorView.post(new Runnable() {
            @Override public void run() {
              printViews(automation.getWindowViews());

              decorView.postDelayed(this, 10000);
            }
          });
        }
      }).start();
    }

    ViewGroup mContentParent = ReflectV.with(activity.getWindow()).injector().field("mContentParent")
        .getQuietly();
    printView(mContentParent);
  }

  static void printView(View view) {
    StringBuilder tabs = new StringBuilder("");
    dumpViewRecursive(view, tabs, null);
  }

  private static void dumpViewRecursive(View view, StringBuilder tabs, StringBuilder dump) {
    // if dump is null, means print log not get string.
    if (view == null) {
      dumpViewInternal(null, tabs, dump);
      return;
    }

    if (view instanceof ViewGroup) {
      dumpViewInternal(view, tabs, dump);

      final ViewGroup group = (ViewGroup) view;
      final int childCount = group.getChildCount();
      for (int i = 0; i < childCount; i++) {
        dumpViewRecursive(group.getChildAt(i), new StringBuilder(tabs).append("    "), dump);
      }

      if (dump == null) {
        VaLogger.d(TAG, tabs.append(closeView(true)).toString());
      } else {
        dump.append(tabs.append(closeView(true)).toString());
      }

      return;
    }

    dumpViewInternal(view, tabs, dump);
  }

  private static void dumpViewInternal(View view, StringBuilder tabs, StringBuilder dump) {
    final String prefix = tabs.toString() + dumpSingleView(view, false);
    final String viewInfo = view instanceof ViewGroup ? prefix : prefix + closeView(false);
    if (dump == null) {
      VaLogger.d(TAG, viewInfo);
    } else {
      dump.append(viewInfo).append('\n');
    }
  }

  static String dumpView(View view) {
    StringBuilder tabs = new StringBuilder("");
    StringBuilder dump = new StringBuilder("");
    dumpViewRecursive(view, tabs, dump);
    return dump.toString();
  }


  public static String dumpSingleView(View view) {
    return dumpSingleView(view, true);
  }

  /*
    <view text: %s, id: %s, type: %s ...></view>
   */
  private static String dumpSingleView(View view, boolean close) {
    final boolean isGroup = view instanceof ViewGroup;
    final String viewFlag = isGroup ? "ViewGroup" : "View";
    final String viewInfo = view == null ? "<view null>" :
        String.format(
            "<%s text: %s, id: %s, type: %s, content-desc: %s, checkable: %s, checked: %s, " +
                "clicked: %s, enable: %s, focusable: %s, focused: %s, long-clickable: %s, selected: %s, hint: %s>",
            viewFlag,
            view instanceof TextView ? ((TextView) view).getText() : "",
            ViewFinder.getId(view),
            view.getClass().getName(),
            view.getContentDescription(),
            Boolean.toString(view instanceof Checkable),
            Boolean.toString(view instanceof Checkable && ((Checkable) view).isChecked()),
            Boolean.toString(view.isClickable()),
            Boolean.toString(view.isEnabled()),
            Boolean.toString(view.isFocusable()),
            Boolean.toString(view.isFocused()),
            Boolean.toString(view.isLongClickable()),
            Boolean.toString(view.isSelected()),
            view instanceof TextView ? ((TextView) view).getHint() : ""
        );
    return close ? viewInfo + closeView(isGroup) : viewInfo;
  }

  private static String closeView(boolean isGroup) {
    return isGroup ? "</ViewGroup>" : "</View>";
  }
}
