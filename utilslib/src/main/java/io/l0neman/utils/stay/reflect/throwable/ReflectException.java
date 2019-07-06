package io.l0neman.utils.stay.reflect.throwable;

/**
 * Created by l0neman on 2019/07/06.
 */
public class ReflectException extends Exception {

  public ReflectException(String message) {
    super(message);
  }

  public ReflectException(Throwable cause) {
    super(cause);
  }

  public ReflectException(String message, Throwable cause) {
    super(message, cause);
  }
}
