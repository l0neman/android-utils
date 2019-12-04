package io.l0neman.utils.stay.uiautomation;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.virtualapp.support.VaLogger;
import io.virtualapp.utils.ReflectV;

/**
 * Created by l0neman on 2019/11/23.
 * todo: stop endless loop vuiobject find works.
 */
public abstract class XUiAutomation {

  private static final String TAG = XUiAutomation.class.getSimpleName();

  private Application mApplication;
  private Context mContext;
  private final Object mWindowManagerGlobal;
  private ReflectV.Injector mViewsInjector;
  private volatile boolean mAppActivityResumed;
  private ExecutorService mAutomationExecutor = Executors.newSingleThreadExecutor();
  private boolean mAppLaunched = false;
  private volatile boolean mOpenNewActivity = true;
  private int mLaunchFlag = 0;

  public XUiAutomation(Context context) {
    this.mContext = context;

    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    mWindowManagerGlobal = wm == null ? null : ReflectV.with(wm).injector().field("mGlobal").getQuietly();
  }

  public void init(Application application) {
    this.mApplication = application;

    application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
      @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        VaLogger.i(TAG, "activity created: " + activity.getClass().getName());
        VaLogger.i(TAG, "activity intent: " + String.valueOf(activity.getIntent()));
        mLaunchFlag++;

        if (!mAppLaunched) {
          mAppLaunched = true;
          appLaunched(activity.getPackageName());
        } else {
          VaLogger.i(TAG, "fuck");
        }
      }

      @Override public void onActivityStarted(Activity activity) {
        if (activity.getClass().getSimpleName().equals("DetailActivity")) {
//          ViewPrinter.printContentViews(activity, VUiAutomation.this);
//          ViewPrinter.printViews(getWindowViews());
        }
      }

      @Override public void onActivityResumed(Activity activity) {
        mAppActivityResumed = true;
        mOpenNewActivity = true;
      }

      @Override public void onActivityPaused(Activity activity) {
        mAppActivityResumed = false;
      }

      @Override public void onActivityStopped(Activity activity) {}

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

      @Override public void onActivityDestroyed(Activity activity) {
        mLaunchFlag--;
        if (mLaunchFlag == 0) {
          mAppLaunched = false;
        }
      }
    });
  }

  public Application getApplication() {
    return mApplication;
  }

  public Context getContext() {
    return mContext;
  }

  public ArrayList<View> getWindowViews() {
    if (mViewsInjector == null) {
      mViewsInjector = ReflectV.with(mWindowManagerGlobal).injector().field("mViews");
    }

    return mViewsInjector.getQuietly();
  }

  boolean isUiAvailable() {
    return !mAppActivityResumed;
  }

  void resetNewActivityFlag() {
    mOpenNewActivity = false;
  }

  boolean isOpenNewActivity() {
    return mOpenNewActivity;
  }

  private void appLaunched(String packageName) {
    mAutomationExecutor.submit(new Runnable() {
      @Override public void run() {
        try {
          onStartAutomation(packageName);
        } catch (Throwable e) {
          e.printStackTrace();
          throw new RuntimeException("shift stack trace.");
        }
      }
    });
  }

  protected abstract void onStartAutomation(String packageName);
}
