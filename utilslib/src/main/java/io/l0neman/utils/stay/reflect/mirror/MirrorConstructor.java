package io.l0neman.utils.stay.reflect.mirror;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;
import io.l0neman.utils.stay.reflect.mirror.util.MethodHelper;

/**
 * Created by l0neman on 2019/07/18.
 * <p>
 * The mapping of the constructor of the target mirror class.
 */
public class MirrorConstructor<T> {
  private Constructor mConstructor;
  // for overload constructors.
  private Map<String, Constructor> mOverloadConstructorMap = new HashMap<>();

  public MirrorConstructor(Constructor[] overloadConstructor) {
    if (overloadConstructor.length == 1) {
      mConstructor = overloadConstructor[0];
    }

    for (Constructor constructor : overloadConstructor) {
      mOverloadConstructorMap.put(MethodHelper.getSignature("c",
          constructor.getParameterTypes()), constructor);
    }
  }

  /**
   * Create an object of the new target mirror type.
   * <p>
   * for no overloaded constructor.
   *
   * @param args constructor parameters.
   * @return new target mirror object.
   * @throws MirrorException otherwise.
   */
  public T newInstance(Object... args) throws MirrorException {
    if (mConstructor == null) {
      return newInstanceOverload((Class[]) null, args);
    }

    try {
      return Reflect.with(mConstructor).create(args);
    } catch (Reflect.ReflectException e) {
      throw new MirrorException("newInstance", e);
    }
  }

  /**
   * @see #newInstanceOverload(Class[], Object...)
   */
  public T newInstanceOverload(String[] parameterTypes, Object... args) throws MirrorException {
    return newInstanceOverload(MethodHelper.getParameterTypes(parameterTypes), args);
  }

  /**
   * Create an object of the new target mirror type.
   * <p>
   * for overloaded constructor.
   *
   * @param parameterTypes constructor parameter type names.
   * @param args           constructor parameters.
   * @return new target mirror object.
   * throws MirrorException otherwise.
   */
  public T newInstanceOverload(Class[] parameterTypes, Object... args) throws MirrorException {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    Constructor constructor = mOverloadConstructorMap.get(MethodHelper.getSignature("c", parameterTypes));

    if (constructor == null) {
      throw new MirrorException("not found method: " + Arrays.toString(parameterTypes));
    }

    try {
      return Reflect.with(constructor).create(args);
    } catch (Exception e) {
      throw new MirrorException(e);
    }
  }
}
