package io.l0neman.utils.stay.reflect.mirror.throwable;

/**
 * Created by l0neman on 2019/07/06.
 */
public class MirrorException extends Exception {

  public MirrorException(String message) {
    super(message);
  }

  public MirrorException(Throwable cause) {
    super(cause);
  }

  public MirrorException(String message, Throwable cause) {
    super(message, cause);
  }
}
