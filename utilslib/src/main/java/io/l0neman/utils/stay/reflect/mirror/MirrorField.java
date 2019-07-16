package io.l0neman.utils.stay.reflect.mirror;


import java.lang.reflect.Field;

import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;

/**
 * Created by l0neman on 2019/07/06.
 */
public class MirrorField<T> {

  private Object mObject;
  private final Field mField;

  // for object field.
  public MirrorField(Object mObject, Field mField) {
    this.mObject = mObject;
    this.mField = mField;
  }

  // for static field.
  public MirrorField(Field mField) {
    this.mField = mField;
  }

  public void setObject(Object mObject) {
    this.mObject = mObject;
  }

  public T get() throws MirrorException {
    try {
      return Reflect.with(mField).targetObject(mObject).get();
    } catch (Reflect.ReflectException e) {
      throw new MirrorException("get field value", e);
    }
  }

  public void set(Object value) throws MirrorException {
    try {
      Reflect.with(mField).targetObject(mObject).set(value);
    } catch (Reflect.ReflectException e) {
      throw new MirrorException("get field value", e);
    }
  }
}
