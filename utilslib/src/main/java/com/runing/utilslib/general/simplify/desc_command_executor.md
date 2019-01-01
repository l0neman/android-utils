# CommandExecutor

[源码 - ICommandExecutor.java](./ICommandExecutor.java) => 接口类
[源码 - CommandExecutor.java](./CommandExecutor.java) => 实现类

方便的命令执行工具，封装了 ProcessBuilder。

## 执行命令同步获取结果

```java
final ICommandExecutor executor = new CommandExecutor();

try {
  executor.execute("ls");
} catch (ICommandExecutor.CommandException e) {
  Log.d(TAG, e.getMessage());
}

new Thread(new Runnable() {
  @Override public void run() {
    /* 使用同步方法读取执行结果，如果命令不中断，则会阻塞 */
    ICommandExecutor.Result result = executor.read();
    Log.d(TAG, "result: " + result.content());
    Log.d(TAG, "exitCode: " + result.exitCode());
    // 或迭代输出内容。
    for (String line : result) {
      Log.d(TAG, "line: " + line);
    }
    // List<String> lines = result.contentList();
    // for (String line : lines) {
    //   Log.d(TAG, "line: " + line);
    // }
  }
}).start();
```

## 执行命令异步获取结果

```java
try {
  executor.execute("ls");
} catch (ICommandExecutor.CommandException e) {
  Log.d(TAG, e.getMessage());
}

/* 使用异步方法读取，回调将在新线程执行。 */
executor.readAsync(new ICommandExecutor.Callback() {
  @Override public void onResultLine(String line) {
    Log.d(TAG, "line: " + line);
  }

  @Override public void onSuccess(ICommandExecutor.Result result) {
    Log.d(TAG, "result: " + result.content());
    Log.d(TAG, "exitCode: " + result.exitCode());
  }
});
```

## 执行命令并在子进程中写入更多命令

写入命令后手动停止。

```java
try {
  executor.execute("su");
  executor.write("ls");
} catch (ICommandExecutor.CommandException e) {
  Log.d(TAG, e.getMessage());
}

new Thread(new Runnable() {
  @Override public void run() {
    ICommandExecutor.Result result = executor.read();
    Log.d(TAG, "result: " + result.content());
    Log.d(TAG, "exitCode: " + result.exitCode());
  }
}).start();

new Thread(new Runnable() {
  @Override public void run() {
    try {
      TimeUnit.SECONDS.sleep(2);
      //  命令可能阻塞且不能自己退出，可手动退出。
      executor.stop();
    } catch (InterruptedException e) {
      System.out.println("sleep interrupted.");
    }
  }
}).start();
```