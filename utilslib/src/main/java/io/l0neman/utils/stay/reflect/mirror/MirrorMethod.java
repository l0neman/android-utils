package io.l0neman.utils.stay.reflect.mirror;

import java.lang.reflect.Method;

import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;

/**
 * Created by l0neman on 2019/07/06.
 */
public class MirrorMethod {

  private Object mObject;
  private final Method mMethod;

  // for object method.
  public MirrorMethod(Object mObject, Method mMethod) {
    this.mObject = mObject;
    this.mMethod = mMethod;
  }

  // for static method.
  public MirrorMethod(Method mMethod) {
    this.mMethod = mMethod;
  }

  public void setObject(Object mObject) {
    this.mObject = mObject;
  }

  public Object invoke(Object... args) throws MirrorException {
    try {
      return Reflect.with(mMethod).targetObject(mObject).invoke(args);
    } catch (Exception e) {
      throw new MirrorException(e);
    }
  }
}
