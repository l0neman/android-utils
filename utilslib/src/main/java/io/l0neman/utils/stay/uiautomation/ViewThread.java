package io.l0neman.utils.stay.uiautomation;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

/**
 * Created by l0neman on 2019/11/28.
 */
class ViewThread {

  private static Handler sHandler = new Handler(Looper.getMainLooper());

  private static class Local<T> {
    T t;

    public synchronized void setT(T t) {
      this.t = t;
    }

    public synchronized T getT() {
      return t;
    }
  }

  interface ValueGetter<T> {
    T getValue();
  }

  private static Handler getViewHandler() {
    return sHandler;
  }

  static <T> T getValueOnViewThread(final ValueGetter<T> valueGetter) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      return valueGetter.getValue();
    }

    final CountDownLatch latch = new CountDownLatch(1);

    final Local<T> local = new Local<>();

    getViewHandler().post(new Runnable() {
      @Override public void run() {
        local.t = valueGetter.getValue();

        latch.countDown();
      }
    });

    try {
      latch.await();
      return local.t;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  static void runOnViewThread(Runnable runnable) {
    runOnViewThread(runnable, false);
  }

  static void runOnViewThreadAsync(Runnable runnable) {
    runOnViewThread(runnable, true);
  }

  private static void runOnViewThread(final Runnable runnable, final boolean isAsync) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      runnable.run();
      return;
    }

    final CountDownLatch latch = isAsync ? null : new CountDownLatch(1);

    getViewHandler().post(new Runnable() {
      @Override public void run() {
        runnable.run();

        if (!isAsync) {
          latch.countDown();
        }
      }
    });

    if (!isAsync) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
