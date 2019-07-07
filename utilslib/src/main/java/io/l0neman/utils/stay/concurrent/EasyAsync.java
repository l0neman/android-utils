package io.l0neman.utils.stay.reflect.concurrent;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * 简单异步处理。
 * <p>
 * 避免内存泄漏。
 * <p>
 * 适用于处理简单异步情况，返回一个结果或不返回结果的操作。
 */
public class EasyAsync {

  private static final String TAG = EasyAsync.class.getSimpleName();

  public interface ExecutorVoid {
    void run();

    void onResult();
  }

  public static AsyncTask<Void, Void, Void> get(final ExecutorVoid executor) {
    return new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... voids) {
        executor.run();
        return null;
      }

      @Override
      protected void onPostExecute(Void t) {
        executor.onResult();
      }
    };
  }

  public interface Executor<T> {
    T run();

    void onResult(T result);
  }

  public static <T> AsyncTask<Void, Void, T> get(final Executor<T> executor) {
    return new AsyncTask<Void, Void, T>() {
      @Override
      protected T doInBackground(Void... voids) {
        return executor.run();
      }

      @Override
      protected void onPostExecute(T t) {
        executor.onResult(t);
      }
    };
  }

  public interface ExecutorWeakVoid<W> {
    void run(W ref);

    void onResult(W ref);
  }

  public static <W> AsyncTask<Void, Void, Void> get(W w, final ExecutorWeakVoid<W> executor) {
    final WeakReference<W> wRef = new WeakReference<>(w);
    return new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... voids) {
        W ref = wRef.get();
        if (ref != null) {
          executor.run(ref);
        }

        return null;
      }

      @Override
      protected void onPostExecute(Void t) {
        W ref = wRef.get();
        if (ref != null) {
          executor.onResult(ref);
        }
      }
    };


  }

  public interface ExecutorWeak<W, T> {
    T run(W ref);

    void onResult(W ref, T result);
  }

  public static <W, T> AsyncTask<Void, Void, T> get(W w, final ExecutorWeak<W, T> executor) {
    final WeakReference<W> wRef = new WeakReference<>(w);
    return new AsyncTask<Void, Void, T>() {
      @Override
      protected T doInBackground(Void... voids) {
        W ref = wRef.get();
        if (ref != null) {
          return executor.run(ref);
        }

        return null;
      }

      @Override
      protected void onPostExecute(T t) {
        W w = wRef.get();
        if (w != null) {
          executor.onResult(w, t);
        }
      }
    };
  }
}
