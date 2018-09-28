# CommandExecutor

[源码 - CommandExecutor.java](./CommandExecutor.java)

方便的命令执行工具，封装了 ProcessBuilder。

- 执行命令，使用同步方法读取结果。

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
  }
}).start();
```

- 执行命令，使用异步方法读取结果。

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

- 执行命令，并在子进程中写入更多命令，并手动停止。

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