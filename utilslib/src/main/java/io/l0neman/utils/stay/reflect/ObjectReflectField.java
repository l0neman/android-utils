package io.l0neman.utils.stay.reflect;

import io.l0neman.utils.stay.reflect.throwable.ReflectException;

/**
 * Created by l0neman on 2019/07/06.
 */
public class ObjectReflectField extends ReflectField {

  public ObjectReflectField(Class<?> mClass, Object mObject, String name) {
    super(mClass, mObject, name);
  }

  public ObjectReflectField(Class<?> mClass, String name) {
    super(mClass, name);
  }

  public Object get() throws ReflectException {
    return super.getValue();
  }

  public void set(Object value) throws ReflectException {
    super.setValue(value);
  }
}
