package io.l0neman.utils.stay.reflect;

import io.l0neman.utils.stay.reflect.throwable.ReflectException;

/**
 * Created by l0neman on 2019/07/06.
 */
public class IntReflectField extends ReflectField {
  public IntReflectField(Class<?> mClass, Object mObject, String mName) {
    super(mClass, mObject, mName);
  }

  public IntReflectField(Class<?> mClass, String mName) {
    super(mClass, mName);
  }

  public void set(Integer value) throws ReflectException {
    super.setValue(value);
  }

  public int get() throws ReflectException {
    return (int) super.getValue();
  }
}
