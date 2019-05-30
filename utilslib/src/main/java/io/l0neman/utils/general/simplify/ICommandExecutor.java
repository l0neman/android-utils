package io.l0neman.utils.general.simplify;

import java.util.List;

public interface ICommandExecutor {

  interface Callback {
    void onResultLine(String line);

    void onSuccess(Result result);
  }

  class CallbackAdapter implements Callback {

    @Override public void onResultLine(String line) {}

    @Override public void onSuccess(Result result) {}
  }

  interface Result extends Iterable<String> {

    String content();

    List<String> contentList();

    int exitCode();
  }

  class CommandException extends Exception {

    public CommandException(Throwable cause) { super(cause); }
  }

  void execute(String... firstCmd) throws CommandException;

  void write(String cmd) throws CommandException;

  Result read();

  void readAsync(Callback callback);

  void stop();
}
