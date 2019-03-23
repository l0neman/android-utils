package com.runing.utilslib.general.concurrent;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorCreatorTest {

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    }
    catch (Exception ignore) {}
  }

  @Test
  public void single() {
    final ThreadPoolExecutor single = ExecutorCreator.single().create();

    single.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    single.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    single.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    single.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    single.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    sleep(60 * 1000);
  }

  @Test
  public void lite() {
    final ExecutorService lite = ExecutorCreator.lite().create();

    lite.execute(new Runnable() {
      @Override public void run() {
        sleep(2 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    lite.execute(new Runnable() {
      @Override public void run() {
        sleep(2 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    sleep(4000);

    lite.execute(new Runnable() {
      @Override public void run() {
        sleep(4 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    lite.execute(new Runnable() {
      @Override public void run() {
        sleep(4 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    lite.execute(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    sleep(60 * 1000);
  }

  @Test
  public void io() {
    final ThreadPoolExecutor io = ExecutorCreator.io()
        .coreSize(1)
        .maxPoolSize(2)
        .maxTaskSize(2)
        .create();
    io.submit(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    io.submit(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    io.submit(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    io.submit(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    io.submit(new Runnable() {
      @Override public void run() {
        sleep(5 * 1000);
        System.out.println("ok " + Thread.currentThread());
      }
    });

    sleep(60 * 1000);
  }

  @Test
  public void custom() {
  }
}