package io.l0neman.utils.stay.reflect.mirror;

import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;

/**
 * Created by l0neman on 2019/07/06.
 */
public class MirrorMethod {

  public Class<?> mClass;
  public Object mObject;
  public String mName;
  public Class<?>[] mParameterTypes;

  // for object's field.
  public MirrorMethod(Class<?> mClass, Object mObject, String mName, Class<?>[] mParameterTypes) {
    this.mClass = mClass;
    this.mObject = mObject;
    this.mName = mName;
    this.mParameterTypes = mParameterTypes;
  }

  // for class's static field.
  public MirrorMethod(Class<?> mClass, String mName, Class<?>[] mParameterTypes) {
    this.mClass = mClass;
    this.mName = mName;
    this.mParameterTypes = mParameterTypes;
  }

  public Object invoke(Object... args) throws MirrorException {
    try {
      return Reflect.with(mObject != null ? mObject : mClass).invoker()
          .method(mName)
          .parameterType(mParameterTypes)
          .invoke();
    } catch (Exception e) {
      throw new MirrorException(e);
    }
  }
}
