package io.l0neman.utils.stay.uiautomation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by l0neman on 2019/11/25.
 */
public class XUiDevice implements XSearchable {

  private static final long WAIT_FOR_IDLE_TIMEOUT = 10 * 1000L;
  private static final long WAIT_FOR_IDLE_POLL = 1000L;

  private static XUiDevice sInstance;
  private XUiAutomation mVUiAutomation;

  private XUiDevice(XUiAutomation mVUiAutomation) {
    this.mVUiAutomation = mVUiAutomation;
  }

  public static XUiDevice getInstance() {
    if (sInstance == null) {
      throw new IllegalStateException("VUiDevice singleton not initialized");
    }

    return sInstance;
  }

  public XUiAutomation getVUiAutomation() {
    return mVUiAutomation;
  }

  public static XUiDevice getInstance(XUiAutomation vUiAutomation) {
    if (sInstance == null) {
      sInstance = new XUiDevice(vUiAutomation);
    }

    return sInstance;
  }

  public void waitForIDLE() {
    waitForIDLE(WAIT_FOR_IDLE_TIMEOUT);
  }

  public void waitForIDLE(long timeout) {
    long beginTime = SystemClock.uptimeMillis();

    while (mVUiAutomation.isUiAvailable()) {
      if (SystemClock.uptimeMillis() - beginTime > timeout) {
        return;
      }

      SystemClock.sleep(WAIT_FOR_IDLE_POLL);
    }
  }

  public XUiObject makeObject(XUiSelector selector) {
    return new XUiObject(this, selector);
  }

  @Override public boolean hasObject(XUiSelector selector) {
    return checkHashObject(selector);
  }

  private boolean checkHashObject(XUiSelector selector) {
    final ArrayList<View> windowViews = ViewThread.getValueOnViewThread(
        new ViewThread.ValueGetter<ArrayList<View>>() {
          @Override public ArrayList<View> getValue() {
            return mVUiAutomation.getWindowViews();
          }
        });

    for (View view : windowViews) {
      if (ViewFinder.findWithView(new XUiObject(XUiDevice.this, view), null, selector)) {
        return true;
      }
    }

    return false;
  }

  @Override public XUiObject findObject(XUiSelector selector) {
    final List<XUiObject> objects = findObjects(selector);
    return objects.isEmpty() ? null : objects.get(0);
  }

  @Override public XUiObject findIndexObject(XUiSelector selector, int index) {
    final List<XUiObject> objects = findObjects(selector);
    return objects.size() <= index ? null : objects.get(index);
  }

  @Override public XUiObject findLastObject(XUiSelector selector) {
    final List<XUiObject> objects = findObjects(selector);
    return objects.isEmpty() ? null : objects.get(objects.size() - 1);
  }

  @Override public List<XUiObject> findObjects(XUiSelector selector) {
    List<XUiObject> results = Collections.synchronizedList(new ArrayList<>());

    ViewThread.runOnViewThread(new Runnable() {
      @Override public void run() {
        ArrayList<View> windowViews = mVUiAutomation.getWindowViews();
        for (View view : windowViews) {
          ViewFinder.findWithView(new XUiObject(XUiDevice.this, view), results, selector);
        }
      }
    });

    return results;
  }

  public void setTextToClipboard(String text) {
    ViewThread.runOnViewThread(new Runnable() {
      @Override public void run() {
        ClipboardManager cm = (ClipboardManager) mVUiAutomation.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        if (cm == null) {
          return;
        }

        cm.setPrimaryClip(ClipData.newPlainText("url", text));
      }
    });
  }

  public void dumpWindowHierarchy(File file) throws IOException {
    dumpWindowHierarchy(new FileOutputStream(file));
  }

  public void dumpWindowHierarchy(OutputStream out) throws IOException {
    final ArrayList<View> views = mVUiAutomation.getWindowViews();
    StringBuilder viewInfo = new StringBuilder();

    for (View view : views) {
      viewInfo.append(
          String.format("root: { type: %s, id: %s }", view.getClass().getName(), ViewFinder.getId(view)))
          .append(ViewPrinter.dumpView(view))
          .append("\n");
    }

    BufferedWriter br = null;
    // noinspection TryFinallyCanBeTryWithResources
    try {
      br = new BufferedWriter(new OutputStreamWriter(out));
      br.write(viewInfo.toString());
    } finally {
      if (br != null) {
        try { br.close(); } catch (IOException ignore) {}
      }
    }
  }

  public <R> R wait(final XSearchCondition<R> searchCondition, long timeout) {
    final long beginTime = SystemClock.uptimeMillis();

    R ret;
    while (true) {
      ret = ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<R>() {
        @Override public R getValue() {
          return searchCondition.apply(XUiDevice.this);
        }
      });

      if (!(ret == null || ret.equals(false)))
        break;

      if (SystemClock.uptimeMillis() - beginTime > timeout) {
        break;
      }
    }

    return ret;
  }

  // future support:
  // click(int x, int y);
  // drag(int startX, int startY, int endX, int endY, ine steps);
  // getCurrentActivityName();
  // getCurrentPackageName();
  // getDisplayHeight();
  // getDisplayWidth();
  // hasObject(UISelector selector);
  // isScreenOn();
  // pressBack();
  // pressDelete();
  // pressEnter();
  // pressHome();
  // pressKeyCode();
  // pressMenu();
  // pressRecentApps();
  // pressSearch();
  // swipe(int startX, int startY, int endX, int endY, ine steps);
  // takeScreenshot(File storePath, float scale, int quality);
  // takeScreenshot(File storePath);
  // wait(Condition<T> condition);
}
