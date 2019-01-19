package com.runing.urilslibtest.general.concurrent;

import com.runing.utilslib.general.concurrent.ExecutorCreator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorCreatorTest {

  private void test() {
    /* 创建单线程池 */
    ThreadPoolExecutor singleThreadPool = ExecutorCreator.single().create();
    /* 创建计算密集型线程池 */
    ThreadPoolExecutor computeThreadPool = ExecutorCreator.compute().create();
    /* 创建 IO 密集型线程池 */
    ThreadPoolExecutor ioThreadPool = ExecutorCreator.io().create();
    /* 创建轻型无限线程池 */
    ThreadPoolExecutor liteThreadPool = ExecutorCreator.lite().create();

    ExecutorCreator.custom()
        .coreSize(2)
        // 允许回收核心线程。
        .allowRecycleCore()
        // 线程空闲时间。
        .keepAliveTime(30L, TimeUnit.SECONDS)
        // 工作队列。
        .workQueue(new ArrayBlockingQueue<Runnable>(2))
        // 线程标签。
        .threadLabel("newPool")
        // 线程池容量。
        .maxPoolSize(3)
        // 线程池容量，通过线程等待时间和计算时间估算。
        .maxPoolSize(20L, 2L)
        // 最大任务数量。
        .maxTaskSize(10)
        // 线程优先级。
        .threadPriority(Thread.MAX_PRIORITY)
        // 线程工厂。
        .threadFactory(new ThreadFactory() {
          @Override public Thread newThread(Runnable runnable) {
            return new Thread(runnable);
          }
        })
        // 线程拒绝处理。
        .rejectedHandler(new RejectedExecutionHandler() {
          @Override
          public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            new Thread(runnable).start();
          }
        });
  }
}
