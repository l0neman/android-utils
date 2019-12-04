package io.l0neman.utils.stay.uiautomation;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by l0neman on 2019/11/23.
 * <p>
 */
public class XUiObject implements XSearchable {

  private static final long WAIT_FOR_SELECTOR_TIMEOUT = 10 * 1000L;
  private static final long WAIT_FOR_SELECTOR_POLL = 1000L;

  private static final long WAIT_FOR_NEW_WINDOW_TIMEOUT = 10 * 1000L;
  private static final long WAIT_FOR_NEW_WINDOW_POLL = 1000L;

  private View mTarget;
  private XUiSelector mSelector;
  private XUiDevice mDevice;

  XUiObject(XUiDevice mDevice, XUiSelector mSelector) {
    this.mDevice = mDevice;
    this.mSelector = mSelector;
  }

  XUiObject(XUiDevice mDevice, View mTarget) {
    this.mDevice = mDevice;
    this.mTarget = mTarget;
  }

  public View getView() {
    checkView();
    return mTarget;
  }

  private void checkView() {
    if (mTarget == null) {
      throw new XUiObjectNotFoundException(mSelector.toString());
    }
  }

  public void click() {
    checkView();
    ViewThread.runOnViewThread(new Runnable() {
      @Override public void run() {
        getView().performClick();
      }
    });
  }

  public void longClick() {
    checkView();
    ViewThread.runOnViewThread(new Runnable() {
      @Override public void run() {
        getView().performLongClick();
      }
    });
  }

  private static void touchClickView(final View target) {
    final Random random = new Random();
    target.post(new Runnable() {
      @Override
      public void run() {
        final int width = target.getWidth();
        final int height = target.getHeight();

        // 触点分布 x 10%~90% 之间。
        final int x = width / 10 + random.nextInt(width / 5 * 4);
        // 触点分布 y 20%~80% 之间。
        final int y = height / 8 + random.nextInt(height / 4 * 3);

        // 抬起时间 100ms + 0~300ms。
        final long downTime = System.currentTimeMillis();
        final long upTime = downTime + 100 + random.nextInt(300);

        final MotionEvent down = MotionEvent.obtain(downTime, downTime + random.nextInt(5),
            MotionEvent.ACTION_DOWN, x, y, MotionEvent.AXIS_PRESSURE);
        target.dispatchTouchEvent(down);

        final MotionEvent up = MotionEvent.obtain(upTime, upTime + random.nextInt(8),
            MotionEvent.ACTION_UP, x, y, MotionEvent.AXIS_PRESSURE);
        target.dispatchTouchEvent(up);

        down.recycle();
        up.recycle();
      }
    });
  }

  public void touchClick() {
    checkView();
    ViewThread.runOnViewThread(new Runnable() {
      @Override public void run() {
        touchClickView(getView());
      }
    });
  }

  public boolean setText(String text) {
    checkView();
    if (getView() instanceof TextView) {
      ViewThread.runOnViewThread(new Runnable() {
        @Override public void run() {
          ((TextView) getView()).setText(text);
        }
      });

      return true;
    }

    return false;
  }

  public XUiObject getParent() {
    try {
      return new XUiObject(mDevice, (View) getView().getParent());
    } catch (ClassCastException e) {
      return null;
    }
  }

  public boolean waitForExists() {
    return waitForExists(WAIT_FOR_SELECTOR_TIMEOUT);
  }

  public boolean waitForExists(long timeout) {
    if (mTarget != null) {
      return true;
    }

    final long beginTime = SystemClock.uptimeMillis();
    while (true) {
      final XUiObject uiObject = mDevice.findObject(mSelector);

      if (uiObject != null) {
        mTarget = uiObject.mTarget;
        return true;
      }

      if (SystemClock.uptimeMillis() - beginTime > timeout) {
        return false;
      }

      SystemClock.sleep(WAIT_FOR_SELECTOR_POLL);
    }
  }

  XUiDevice getDevice() {
    return mDevice;
  }

  public XUiObject makeObject(XUiSelector selector) {
    return new XUiObject(mDevice, selector);
  }

  @Override public boolean hasObject(XUiSelector selector) {
    return checkHashObject(selector);
  }

  private boolean checkHashObject(XUiSelector selector) {
    return ViewFinder.findWithView(XUiObject.this, null, selector);
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
    checkView();
    List<XUiObject> results = new ArrayList<>();
    ViewFinder.findWithView(XUiObject.this, results, selector);
    return results;
  }

  public CharSequence getText() {
    checkView();
    return getView() instanceof TextView ?
        ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<CharSequence>() {
          @Override public CharSequence getValue() {
            return ((TextView) getView()).getText();
          }
        }) : "";
  }

  public CharSequence getContentDesc() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<CharSequence>() {
      @Override public CharSequence getValue() {
        return getView().getContentDescription();
      }
    });
  }

  public CharSequence getHint() {
    checkView();
    return getView() instanceof EditText ?
        ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<CharSequence>() {
          @Override public CharSequence getValue() {
            return ((EditText) getView()).getHint();
          }
        }) : "";
  }

  public boolean isCheckable() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView() instanceof Checkable;
      }
    });
  }

  public boolean isChecked() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView() instanceof Checkable && ((Checkable) getView()).isChecked();
      }
    });
  }

  public boolean isClickable() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isClickable();
      }
    });
  }

  public boolean isEnabled() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isEnabled();
      }
    });
  }

  public boolean isFocusable() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isFocusable();
      }
    });
  }

  public boolean isFocused() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isFocused();
      }
    });
  }

  public boolean isLongClickable() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isLongClickable();
      }
    });
  }

  public boolean isSelected() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<Boolean>() {
      @Override public Boolean getValue() {
        return getView().isSelected();
      }
    });
  }

  public Class<?> getType() {
    checkView();
    return getView().getClass();
  }

  public String getResourceId() {
    checkView();
    return ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<String>() {
      @Override public String getValue() {
        return ViewFinder.getId(getView());
      }
    });
  }

  public <R> R wait(final XSearchCondition<R> searchCondition, long timeout) {
    final long beginTime = SystemClock.uptimeMillis();

    R ret;
    while (true) {
      ret = ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<R>() {
        @Override public R getValue() {
          return searchCondition.apply(XUiObject.this);
        }
      });

      if (!(ret == null || ret.equals(false))) { break; }

      if (SystemClock.uptimeMillis() - beginTime > timeout) { break; }
    }

    return ret;
  }

  public <R> R wait(final XUiObjectCondition<R> uiObjectCondition, long timeout) {
    final long beginTime = SystemClock.uptimeMillis();

    R ret;
    while (true) {
      ret = ViewThread.getValueOnViewThread(new ViewThread.ValueGetter<R>() {
        @Override public R getValue() {
          return uiObjectCondition.apply(XUiObject.this);
        }
      });

      if (!(ret == null || ret.equals(false))) { break; }

      if (SystemClock.uptimeMillis() - beginTime > timeout) { break; }
    }

    return ret;
  }

  public boolean clickAndWaitForNewWindow() {
    checkView();
    return clickAndWaitForNewWindow(WAIT_FOR_NEW_WINDOW_TIMEOUT);
  }

  public boolean clickAndWaitForNewWindow(long timeout) {
    final XUiAutomation vUiAutomation = mDevice.getVUiAutomation();

    vUiAutomation.resetNewActivityFlag();
    click();

    final long beginTime = SystemClock.uptimeMillis();

    while (!vUiAutomation.isOpenNewActivity()) {
      SystemClock.sleep(WAIT_FOR_NEW_WINDOW_POLL);

      if (SystemClock.uptimeMillis() - beginTime > timeout) {
        return false;
      }
    }

    return true;
  }

  public void recycle() {
    mSelector = null;
    mTarget = null;
  }

  // future support:
  // from androidx UiObject.
  // dragTo(...);
  // exists();
  // getBounds();
  // getChild(UiSelector selector);
  // getChildCount();
  // isScrollable();
  // performMultiPointerGesture(...);
  // pinchIn(int percent, int steps);
  // pinchOut(int percent, int steps);
  // swipe[down, up, left, down]

  // future support:
  // from androidx UiObject2;
  // clickAndWait(Condition);
  // drag();
  // fling();
  // scroll();
  // getApplicationPackage();
  // getChildren();
  // wait(Condition);
}
