package com.runing.utilslib.general.conurrent;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ExecutorCreator {

  private static final int CORE_NUMBER = Math.min(Runtime.getRuntime().availableProcessors(), 4);
  private static final int COMPUTE_POOL_SIZE = CORE_NUMBER + 1;
  private static final int IO_POOL_SIZE = CORE_NUMBER * 2 + 1;
  private static final long DEFAULT_KEEP_ALIVE_TIME = 30L;
  private static ThreadLocal<Creator> sCreatorInstance;

  /**
   * 清理工具对象（洁癖）.
   */
  public static void recycle() {
    sCreatorInstance = null;
  }

  private static Creator apply() {
    if (sCreatorInstance == null) {
      sCreatorInstance = new ThreadLocal<>();
    }
    Creator creator = sCreatorInstance.get();
    if (creator == null) {
      creator = new Creator();
      return creator;
    }
    creator.clear();
    return creator;
  }

  /**
   * IO 密集型，核心线程为 CPU 数，最大线程数为 CPU * 2 + 1。
   */
  public static Creator io() {
    return apply()
        .coreSize(CORE_NUMBER)
        .maxPoolSize(IO_POOL_SIZE)
        .keepAliveTime(DEFAULT_KEEP_ALIVE_TIME)
        .threadLabel("io")
        .threadPriority(Thread.NORM_PRIORITY - 1);
  }

  /**
   * 计算密集型，核心线程为 CPU 数，最大线程数为 CPU + 1。
   */
  public static Creator compute() {
    return new Creator()
        .coreSize(CORE_NUMBER)
        .maxPoolSize(COMPUTE_POOL_SIZE)
        .keepAliveTime(DEFAULT_KEEP_ALIVE_TIME)
        .threadLabel("compute")
        .threadPriority(Thread.NORM_PRIORITY + 1);
  }

  /**
   * 轻型，无核心线程，默认无限线程数量。
   */
  public static Creator lite() {
    return new Creator()
        .coreSize(0)
        .maxPoolSize(Integer.MAX_VALUE)
        .workQueue(new SynchronousQueue<Runnable>(true))
        .threadLabel("lite")
        .threadPriority(Thread.NORM_PRIORITY - 2);
  }

  /**
   * 单线程。
   */
  public static Creator single() {
    return new Creator()
        .coreSize(1)
        .maxPoolSize(1)
        .threadLabel("single")
        .threadPriority(Thread.NORM_PRIORITY);
  }

  /**
   * 自定义。
   */
  public static Creator custom() {
    return new Creator();
  }

  public static final class Creator {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private int mCorePoolSize = 0;
    private int mMaxPoolSize = 0;
    private int mMaxTaskSize = 0;
    private boolean isRecycleCore = false;
    private long mKeepAliveTime = 0;
    private BlockingQueue<Runnable> mWorkQueue;
    private ThreadFactory mThreadFactory;
    private RejectedExecutionHandler mHandler;
    private String mThreadLabel = "thread pool" + poolNumber.getAndDecrement();
    private int mThreadPriority = Thread.NORM_PRIORITY - 1;

    public void clear() {
      poolNumber.set(0);
      mCorePoolSize = 0;
      mMaxPoolSize = 0;
      isRecycleCore = false;
      mKeepAliveTime = 0;
      mWorkQueue = null;
      mThreadFactory = null;
      mHandler = null;
      mThreadLabel = "thread pool" + poolNumber.getAndDecrement();
      mThreadPriority = Thread.NORM_PRIORITY - 1;
    }

    /**
     * 核心线程数。
     */
    public Creator coreSize(int n) {
      mCorePoolSize = n;
      return this;
    }

    /**
     * 通过 [ 线程等待时间 ] 和 [ 执行时间 ] 推算线程池大小。
     */
    public Creator maxPoolSize(long waitTime, long calculateTime) {
      mMaxPoolSize = (int) (CORE_NUMBER * (1 + waitTime * 1F / calculateTime));
      return this;
    }

    /**
     * 最大线程数。
     */
    public Creator maxPoolSize(int n) {
      mMaxPoolSize = n;
      return this;
    }

    /**
     * 最大任务队列数。
     */
    public Creator maxTaskSize(int n) {
      mMaxTaskSize = n;
      return this;
    }

    /**
     * 线程标签，用于初始化默认线程工厂。
     */
    public Creator threadLabel(String threadLabel) {
      mThreadLabel = threadLabel;
      return this;
    }

    /**
     * 线程优先级，用于初始化默认线程工厂。
     */
    public Creator threadPriority(int threadPriority) {
      mThreadPriority = threadPriority;
      return this;
    }

    /**
     * 允许回收核心线程。
     */
    public Creator allowRecycleCore() {
      isRecycleCore = true;
      return this;
    }

    /**
     * 超时时间。
     */
    public Creator keepAliveTime(long time) {
      mKeepAliveTime = time;
      return this;
    }

    /**
     * 指定任务队列。
     */
    public Creator workQueue(BlockingQueue<Runnable> workQueue) {
      this.mWorkQueue = workQueue;
      return this;
    }

    private static final class DefaultThreadFactory implements ThreadFactory {

      private final ThreadGroup group;
      private final AtomicInteger threadNumber = new AtomicInteger(1);
      private final String namePrefix;
      private final int priority;

      DefaultThreadFactory(final String flag, int priority) {
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
            Thread.currentThread().getThreadGroup();
        namePrefix = flag + ": thread-" + threadNumber.getAndIncrement();
      }

      @Override
      public Thread newThread(@NonNull Runnable r) {
        final Thread thread = new Thread(group, r, namePrefix, 0);
        thread.setPriority(priority);
        return thread;
      }
    }

    /**
     * 指定线程工厂。
     */
    public Creator threadFactory(ThreadFactory threadFactory) {
      this.mThreadFactory = threadFactory;
      return this;
    }

    /**
     * 任务拒绝处理。
     */
    public Creator rejectedHandler(RejectedExecutionHandler handler) {
      this.mHandler = handler;
      return this;
    }

    public ThreadPoolExecutor create() {
      if (mHandler == null) { mHandler = new ThreadPoolExecutor.AbortPolicy(); }

      if (mWorkQueue == null) {
        if (mMaxTaskSize != 0) {
          mWorkQueue = new ArrayBlockingQueue<>(mMaxTaskSize);
        }
        else {
          mWorkQueue = new LinkedBlockingQueue<>();
        }
      }

      if (mThreadFactory == null) {
        mThreadFactory = new DefaultThreadFactory(mThreadLabel, mThreadPriority);
      }

      final ThreadPoolExecutor executor = new ThreadPoolExecutor(mCorePoolSize, mMaxPoolSize,
          mKeepAliveTime, TimeUnit.SECONDS, mWorkQueue, mThreadFactory, mHandler);

      if (isRecycleCore) { executor.allowCoreThreadTimeOut(true); }

      return executor;
    }
  }
}
