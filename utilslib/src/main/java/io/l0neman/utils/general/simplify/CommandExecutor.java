package io.l0neman.utils.general.simplify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CommandExecutor implements ICommandExecutor {

  private ExecutorService exec = Executors.newSingleThreadExecutor();
  private Process process;

  public static final class Result implements ICommandExecutor.Result {

    private final List<String> lines;
    private final String content;
    private final int exitValue;

    public Result(List<String> lines, String content, int exitValue) {
      this.lines = lines;
      this.content = content;
      this.exitValue = exitValue;
    }

    @Override public String content() {
      return content;
    }

    @Override public List<String> contentList() {
      return lines;
    }

    @Override public int exitCode() {
      return exitValue;
    }

    @Override public Iterator<String> iterator() {
      return lines.iterator();
    }
  }

  @Override
  public void execute(String... firstCmd) throws CommandException {
    ProcessBuilder pb = new ProcessBuilder(firstCmd)
        .redirectErrorStream(true);
    try {
      this.process = pb.start();
    } catch (IOException e) {
      throw new CommandException(e);
    }
  }

  @Override public void write(String cmd) throws CommandException {
    try {
      OutputStream os = this.process.getOutputStream();
      os.write(cmd.getBytes());
      os.write("\n".getBytes());
      os.flush();
    } catch (IOException e) {
      throw new CommandException(e);
    }
  }

  @Override public ICommandExecutor.Result read() {
    LinkedList<String> lines = new LinkedList<>();
    String content = getContent(lines, this.process, null);

    int exitValue;
    try {
      exitValue = this.process.waitFor();
    } catch (InterruptedException ignore) {
      exitValue = -1;
    }

    return new Result(lines, content, exitValue);
  }

  @Override public void readAsync(final Callback callback) {
    //noinspection Convert2Lambda
    final CommandExecutor ce = this;
    exec.execute(new Runnable() {
      @Override
      public void run() {
        LinkedList<String> lines = new LinkedList<>();
        String content = getContent(lines, ce.process, callback);

        int exitValue;
        try {
          exitValue = ce.process.waitFor();
        } catch (InterruptedException e) {
          exitValue = -1;
        }

        callback.onSuccess(new Result(lines, content, exitValue));
      }
    });
  }

  private static String getContent(List<String> lines, Process process, Callback callback) {
    try {
      Scanner scanner = new Scanner(process.getInputStream());
      StringBuilder builder = new StringBuilder();

      while (scanner.hasNext()) {
        String nextLine = scanner.nextLine();

        if (nextLine.length() != 0) {
          builder.append(nextLine).append('\n');
          if (callback != null) {
            callback.onResultLine(nextLine);
          }
          lines.add(nextLine);
        }
      }

      return builder.toString();
    } catch (IllegalStateException ignore) {}
    return "";
  }

  @Override public void stop() {
    exec.shutdownNow();

    if (process == null) {
      return;
    }

    process.destroy();

    try {
      process.getInputStream().close();
    } catch (IOException ignore) {}

    try {
      process.getOutputStream().close();
    } catch (IOException ignore) {}
  }
}
