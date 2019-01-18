# ExecutorCreator

[源码 - ExecutorCreator.java](./ExecutorCreator.java)

线程池工具构建器，提供默认的典型线程池，也可使用链式方法构造自定义线程池。

## 1. 创建标准默认线程池。

```java
/* 创建单线程池 */
ThreadPoolExecutor singleThreadPool = ExecutorCreator.single().create();

/* 创建计算密集型线程池 */
ThreadPoolExecutor computeThreadPool = ExecutorCreator.compute().create();

/* 创建 IO 密集型线程池 */
ThreadPoolExecutor ioThreadPool = ExecutorCreator.io().create();

/* 创建轻型无限线程池 */
ThreadPoolExecutor liteThreadPool = ExecutorCreator.lite().create();
```

## 2. 创建自定义线程池，将构造器参数转为链式，更直观方便。

```java
ExecutorCreator.custom()
    // 核心线程数量。
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
```