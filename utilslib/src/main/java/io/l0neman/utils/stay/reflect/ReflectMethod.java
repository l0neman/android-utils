package io.l0neman.utils.stay.reflect;

import io.l0neman.utils.stay.reflect.throwable.ReflectException;
import io.l0neman.utils.stay.reflect.util.Reflect;

/**
 * Created by l0neman on 2019/07/06.
 */
public class ReflectMethod {

  public Class<?> mClass;
  public Object mObject;
  public String mName;
  public Class<?>[] mParameterTypes;

  // for object's field.
  public ReflectMethod(Class<?> mClass, Object mObject, String mName, Class<?>[] mParameterTypes) {
    this.mClass = mClass;
    this.mObject = mObject;
    this.mName = mName;
    this.mParameterTypes = mParameterTypes;
  }

  // for class's static field.
  public ReflectMethod(Class<?> mClass, String mName, Class<?>[] mParameterTypes) {
    this.mClass = mClass;
    this.mName = mName;
    this.mParameterTypes = mParameterTypes;
  }

  public Object invoke(Object... args) throws ReflectException {
    try {
      return Reflect.with(mClass).invoker()
          .targetObject(mObject)
          .method(mName)
          .paramsType(mParameterTypes)
          .invoke();
    } catch (Exception e) {
      throw new ReflectException(e);
    }
  }
}
