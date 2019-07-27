package io.l0neman.utils.stay.reflect.mirror;


import java.lang.reflect.Field;

import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;

/**
 * Created by l0neman on 2019/07/06.
 * <p>
 * The mapping of the field of the target mirror class.
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

  /**
   * Set the target call object.
   *
   * @param mObject target call object.
   */
  public void setObject(Object mObject) {
    this.mObject = mObject;
  }

  /**
   * get field value.
   *
   * @return field's value.
   * @throws MirrorException otherwise.
   */
  public T get() throws MirrorException {
    try {
      return Reflect.with(mField).targetObject(mObject).get();
    } catch (Reflect.ReflectException e) {
      throw new MirrorException("getSignature field value", e);
    }
  }

  /**
   * set field value.
   *
   * @param value new value.
   * @throws MirrorException otherwise.
   */
  public void set(T value) throws MirrorException {
    try {
      Reflect.with(mField).targetObject(mObject).set(value);
    } catch (Reflect.ReflectException e) {
      throw new MirrorException("getSignature field value", e);
    }
  }
}
