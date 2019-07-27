package io.l0neman.utils.stay.reflect.mirror;

import androidx.collection.ArrayMap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;
import io.l0neman.utils.stay.reflect.mirror.util.MethodHelper;

/**
 * Created by l0neman on 2019/07/06.
 * <p>
 * The mapping of the constructor of the target mirror classe.
 */
public class MirrorMethod<T> {

  private Object mObject;
  private Method mMethod;
  private Map<String, Method> mOverloadMethodMap = new ArrayMap<>();

  // for object method.
  public MirrorMethod(Object mObject, Method[] overloadMethod) {
    this.mObject = mObject;
    process(overloadMethod);
  }

  // for static method.
  public MirrorMethod(Method[] overloadMethod) {
    process(overloadMethod);
  }

  private void process(Method[] overloadMethod) {
    if (overloadMethod.length == 1) {
      mMethod = overloadMethod[0];
    }

    for (Method method : overloadMethod) {
      mOverloadMethodMap.put(MethodHelper.getSignature("o", method.getParameterTypes()), method);
    }
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
   * Method of calling a target mirror object.
   * <p>
   * for no overloaded method.
   *
   * @param args method parameters.
   * @return method return value.
   * @throws MirrorException otherwise.
   */
  public T invoke(Object... args) throws MirrorException {
    if (mMethod == null) {
      return invokeOverload((Class[]) null, args);
    }

    try {
      return Reflect.with(mMethod).targetObject(mObject).invoke(args);
    } catch (Exception e) {
      throw new MirrorException(e);
    }
  }

  /**
   * @see #invokeOverload(Class[], Object...)
   */
  public T invokeOverload(String[] parameterTypes, Object... args) throws MirrorException {
    return invokeOverload(MethodHelper.getParameterTypes(parameterTypes), args);
  }

  /**
   * Method of calling a target mirror object.
   * <p>
   * for overloaded method.
   *
   * @param parameterTypes method parameter type names.
   * @param args           method parameters.
   * @return method return value.
   * @throws MirrorException otherwise.
   */
  public T invokeOverload(Class[] parameterTypes, Object... args) throws MirrorException {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    Method method = mOverloadMethodMap.get(MethodHelper.getSignature("o", parameterTypes));
    if (method == null) {
      throw new MirrorException("not found method: " + Arrays.toString(parameterTypes));
    }

    try {
      return Reflect.with(method).targetObject(mObject).invoke(args);
    } catch (Exception e) {
      throw new MirrorException(e);
    }
  }
}
