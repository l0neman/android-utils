package io.l0neman.utils.stay.reflect.mirror;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.l0neman.utils.stay.reflect.mirror.annoation.TargetMirrorClass;
import io.l0neman.utils.stay.reflect.mirror.annoation.TargetMirrorClassName;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;
import io.l0neman.utils.stay.reflect.mirror.util.MethodHelper;
import io.l0neman.utils.stay.reflect.mirror.util.MirrorClassInfo;

/**
 * Created by l0neman on 2019/07/19.
 * <p>
 * Target mirror class.
 */
public class MirrorClass<M> {
  private static Map<Class<?>, MirrorClassInfo> sReflectClassesInfoCache = new ConcurrentHashMap<>();

  M mTargetMirrorObject;

  // empty args placeholder.
  private static Class<?>[] ARGS_PLACEHOLDER = new Class[0];

  // class helper:

  public static class InvokeRuntimeException extends RuntimeException {

    public InvokeRuntimeException(Throwable cause) { super(cause); }
  }

  public M getTargetMirrorObject() {
    return mTargetMirrorObject;
  }

  protected static Class<?>[] $(Class<?>... parameterTypes) {
    return parameterTypes;
  }

  protected static Class<?>[] $(String... parameterTypes) {
    return MethodHelper.getParameterTypes(parameterTypes);
  }

  /**
   * for no parameter constructor.
   *
   * @see #construct(Class, Class[], Object...)
   */
  protected void construct(Class<? extends MirrorClass> mirrorClass) throws InvokeRuntimeException {
    construct(mirrorClass, null);
  }

  /**
   * When you need to map the constructor of the target mirror class,
   * you need to call this method to construct and map the target object.
   *
   * @param mirrorClass    mirror class.
   * @param parameterTypes constructor parameterTypes.
   * @param args           constructor parameters.
   * @throws InvokeRuntimeException otherwise.
   */
  protected void construct(Class<? extends MirrorClass> mirrorClass, Class<?>[] parameterTypes,
                           Object... args)
      throws InvokeRuntimeException {

    if (parameterTypes == null) {
      parameterTypes = ARGS_PLACEHOLDER;
    }

    MirrorClassInfo reflectClassInfo = sReflectClassesInfoCache.get(this.getClass());

    final boolean isMapped = reflectClassInfo != null;

    String methodSignature = MethodHelper.getSignature("c", parameterTypes);

    Class<?> targetMirrorClass;
    try {
      targetMirrorClass = isMapped ? reflectClassInfo.getTargetMirrorClass() : getTargetMirrorClass(mirrorClass);
    } catch (MirrorException e) {
      throw new RuntimeException(e);
    }

    Constructor constructor = isMapped ? reflectClassInfo.getConstructor(methodSignature) : null;

    if (constructor == null) {
      try {
        constructor = Reflect.with(targetMirrorClass).creator()
            .parameterTypes(parameterTypes).getConstructor();
      } catch (Reflect.ReflectException e) {
        throw new RuntimeException(e);
      }
    }

    Object targetMirrorObject;
    try {
      targetMirrorObject = Reflect.with(constructor).create(args);

    } catch (Reflect.ReflectException e) {
      if (e.getCause() instanceof InvocationTargetException) {
        throw new InvokeRuntimeException(e.getCause());
      }

      throw new RuntimeException(e);
    }

    try {
      // noinspection unchecked
      map(targetMirrorObject, (Class<MirrorClass>) this.getClass(), this);
    } catch (MirrorException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * for no parameter method.
   *
   * @see #invoke(String, Class[], Object...)
   */
  protected <T> T invoke(String methodName) throws InvokeRuntimeException {
    return invoke(methodName, (Class<?>[]) null);
  }

  /**
   * You need to call this method to provide method information when you need to
   * map an instance method of a target mirror class.
   *
   * @param methodName     method name.
   * @param parameterTypes method parameterTypes.
   * @param args           method parameters.
   * @return The return value of the target mapping method
   */
  protected <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args)
      throws InvokeRuntimeException {
    if (parameterTypes == null) {
      parameterTypes = ARGS_PLACEHOLDER;
    }

    final MirrorClassInfo reflectClassInfo = sReflectClassesInfoCache.get(this.getClass());

    String methodSignature = MethodHelper.getSignature(methodName, parameterTypes);
    Method method = reflectClassInfo.getMethod(methodSignature);

    if (method == null) {
      try {
        method = Reflect.with(mTargetMirrorObject).invoker().method(methodName)
            .paramsType(parameterTypes).getMethod();
      } catch (Reflect.ReflectException e) {
        throw new RuntimeException(e);
      }

      reflectClassInfo.putMethod(methodName, method);
    }

    try {
      return Reflect.with(method).targetObject(mTargetMirrorObject).invoke(args);
    } catch (Reflect.ReflectException e) {
      if (e.getCause() instanceof InvocationTargetException) {
        throw new InvokeRuntimeException(e.getCause());
      }

      throw new RuntimeException(e);
    }
  }

  /**
   * @see #invoke(String, Class[], Object...)
   */
  protected <T> T invoke(String methodName, String[] parameterTypes, Object... args)
      throws InvokeRuntimeException {
    return invoke(methodName, $(parameterTypes), args);
  }

  /**
   * for no parameter static method.
   *
   * @see #invokeStatic(Class, String, Class[], Object...)
   */
  protected static <T> T invokeStatic(Class<? extends MirrorClass> mirrorClass, String methodName)
      throws InvokeRuntimeException {

    return invokeStatic(mirrorClass, methodName, (Class<?>[]) null);
  }

  /**
   * @see #invokeStatic(Class, String, Class[], Object...)
   */
  protected static <T> T invokeStatic(Class<? extends MirrorClass> mirrorClass, String methodName,
                                      String[] parameterTypes, Object... args) {
    return invokeStatic(mirrorClass, methodName, $(parameterTypes), args);
  }

  /**
   * You need to call this method to provide method information when you need to
   * map a static method of the target mirror class.
   *
   * @param mirrorClass    mirror class
   * @param methodName     method name.
   * @param parameterTypes method parameterTypes.
   * @param args           method parameters.
   * @return The return value of the target mapping method
   */
  protected static <T> T invokeStatic(Class<? extends MirrorClass> mirrorClass, String methodName,
                                      Class<?>[] parameterTypes, Object... args)
      throws InvokeRuntimeException {

    if (parameterTypes == null) {
      parameterTypes = ARGS_PLACEHOLDER;
    }

    final MirrorClassInfo reflectClassInfo = sReflectClassesInfoCache.get(mirrorClass);

    String methodSignature = MethodHelper.getSignature(methodName, parameterTypes);
    Method method = reflectClassInfo.getMethod(methodSignature);

    Class<?> targetMirrorClass = reflectClassInfo.getTargetMirrorClass();

    if (method == null) {
      try {
        method = Reflect.with(targetMirrorClass).invoker().method(methodName)
            .paramsType(parameterTypes).getMethod();
      } catch (Reflect.ReflectException e) {
        throw new RuntimeException(e);
      }

      reflectClassInfo.putMethod(methodName, method);
    }

    try {
      return Reflect.with(method).invoke(args);
    } catch (Reflect.ReflectException e) {
      if (e.getCause() instanceof InvocationTargetException) {
        throw new InvokeRuntimeException(e.getCause());
      }

      throw new RuntimeException(e);
    }
  }

  // mirror annotation utils:

  // get mirror class from annotation.
  private static Class<?> getTargetMirrorClass(Class<? extends MirrorClass> mirrorClass)
      throws MirrorException {

    final TargetMirrorClassName mirrorClassName = mirrorClass.getAnnotation(TargetMirrorClassName.class);

    if (mirrorClassName != null) {
      return Reflect.with(mirrorClassName.value()).getClazz();
    }

    final TargetMirrorClass targetMirrorClass = mirrorClass.getAnnotation(TargetMirrorClass.class);

    if (targetMirrorClass != null) {
      return targetMirrorClass.value();
    }

    throw new MirrorException("not get mirror class name, please add annotation. for mirror class: " +
        mirrorClass.getSimpleName());
  }

  // mirror utils:

  /**
   * Map all members of the target map class.
   * <p>
   *
   * @param targetMirrorObject target mirror class instance.
   * @param mirrorClass        mirror class.
   * @param <T>                subclass of MirrorClass
   * @return new mirror class instance that completes the mapping.
   *
   * @throws MirrorException otherwise.
   */
  public static <T extends MirrorClass> T map(Object targetMirrorObject, Class<T> mirrorClass)
      throws MirrorException {

    try {
      T mirrorObject = targetMirrorObject == null ? null : mirrorClass.newInstance();
      map(targetMirrorObject, mirrorClass, mirrorObject);
      return mirrorObject;
    } catch (Throwable e) {
      throw new MirrorException("mirror", e);
    }
  }

  private static <T extends MirrorClass> void map(Object targetMirrorObject, Class<T> mirrorClass,
                                                  T mirrorObject) throws MirrorException {
    MirrorClassInfo reflectClassInfo = sReflectClassesInfoCache.get(mirrorClass);
    boolean isMapped = reflectClassInfo != null;
    boolean forClass = targetMirrorObject == null;

    if (reflectClassInfo == null) {
      reflectClassInfo = new MirrorClassInfo();
      sReflectClassesInfoCache.put(mirrorClass, reflectClassInfo);
    }

    Class<?> targetMirrorClass;
    if (isMapped) {
      targetMirrorClass = reflectClassInfo.getTargetMirrorClass();
    } else {
      targetMirrorClass = getTargetMirrorClass(mirrorClass);
      reflectClassInfo.setTargetMirrorClass(targetMirrorClass);
    }

    try {
      // mapped by constructor.
      if (!forClass && mirrorObject.mTargetMirrorObject != null) {
        return;
      }

      if (!forClass) {
        mirrorObject.mTargetMirrorObject = targetMirrorObject;
      }

      for (Field field : mirrorClass.getDeclaredFields()) {
        final Class<?> fieldType = field.getType();
        final boolean isStatic = Modifier.isStatic(field.getModifiers());

        // skip constants.
        if (isStatic && Modifier.isFinal(field.getModifiers())) {
          continue;
        }

        // for mirror constructor.
        if (MirrorConstructor.class.isAssignableFrom(fieldType)) {
          Constructor[] targetMirrorOverloadConstructor;

          final Class<?>[][] mompt = MethodHelper.getMirrorOverloadMethodParameterTypes(field);

          if (isMapped) {
            targetMirrorOverloadConstructor = new Constructor[mompt.length];

            for (int i = 0; i < mompt.length; i++) {
              String methodSign = MethodHelper.getSignature(field.getName(), mompt[i]);

              targetMirrorOverloadConstructor[i] = reflectClassInfo.getConstructor(methodSign);
            }
          } else {
            targetMirrorOverloadConstructor = new Constructor[mompt.length];

            for (int i = 0; i < mompt.length; i++) {
              String methodSign = MethodHelper.getSignature(field.getName(), mompt[i]);

              final Constructor mirrorConstructor = Reflect.with(targetMirrorClass).creator()
                  .parameterTypes(mompt[i]).getConstructor();

              targetMirrorOverloadConstructor[i] = mirrorConstructor;
              reflectClassInfo.putConstructor(methodSign, mirrorConstructor);
            }
          }

          if (isStatic && forClass) {
            field.set(null, new MirrorConstructor(targetMirrorOverloadConstructor));
          } else if (!isStatic && !forClass) {
            field.set(mirrorObject, new MirrorConstructor(targetMirrorOverloadConstructor));
          }

          continue; // end mirror constructor.
        }

        // for mirror field.
        if (MirrorField.class.isAssignableFrom(fieldType)) {
          Field targetMirrorField;

          if (isMapped) {
            targetMirrorField = reflectClassInfo.getField(field.getName());
          } else {
            targetMirrorField = Reflect.with(targetMirrorClass).injector().field(field.getName())
                .getField();

            reflectClassInfo.putField(field.getName(), targetMirrorField);
          }

          if (isStatic && forClass) {
            field.set(null, new MirrorField(targetMirrorField));
          } else if (!isStatic && !forClass) {
            field.set(mirrorObject, new MirrorField(targetMirrorObject, targetMirrorField));
          }

          continue; // end mirror field.
        }

        // for mirror method.
        if (MirrorMethod.class.isAssignableFrom(fieldType)) {
          Method[] targetMirrorOverloadMethod;

          final Class<?>[][] mompt = MethodHelper.getMirrorOverloadMethodParameterTypes(field);

          if (isMapped) {
            targetMirrorOverloadMethod = new Method[mompt.length];

            for (int i = 0; i < mompt.length; i++) {
              String methodSign = MethodHelper.getSignature(field.getName(), mompt[i]);

              targetMirrorOverloadMethod[i] = reflectClassInfo.getMethod(methodSign);
            }
          } else {
            targetMirrorOverloadMethod = new Method[mompt.length];

            for (int i = 0; i < mompt.length; i++) {
              String methodSign = MethodHelper.getSignature(field.getName(), mompt[i]);

              final Method mirrorMethod = Reflect.with(targetMirrorClass).invoker()
                  .method(field.getName())
                  .paramsType(mompt[i]).getMethod();

              targetMirrorOverloadMethod[i] = mirrorMethod;
              reflectClassInfo.putMethod(methodSign, mirrorMethod);
            }
          }

          if (isStatic && forClass) {
            field.set(null, new MirrorMethod(targetMirrorOverloadMethod));
          } else if (!isStatic && !forClass) {
            field.set(mirrorObject, new MirrorMethod(targetMirrorObject, targetMirrorOverloadMethod));
          }

          continue; // end mirror method.
        }

        // for MirrorClass.
        if (MirrorClass.class.isAssignableFrom(fieldType)) {

          final Object targetMirrorField = isStatic && forClass ? null :
              Reflect.with(targetMirrorClass).injector().field(field.getName())
                  .targetObject(targetMirrorObject).get();

          // noinspection unchecked
          final MirrorClass mapClass = MirrorClass.map(targetMirrorField,
              (Class<? extends MirrorClass>) fieldType);

          if (isStatic && forClass) {
            field.set(null, mapClass);
          } else if (!isStatic && !forClass) {
            field.set(mirrorObject, mapClass);
          }

        }
      }
    } catch (Exception e) {
      throw new MirrorException("map", e);
    }
  }

  /**
   * ignore exceptions.
   *
   * @see #map(Class)
   */
  public static void mapQuiet(Class<? extends MirrorClass> mirrorClass) {
    try {
      map(null, mirrorClass);
    } catch (MirrorException e) {
      Log.w("mirror", "mirror for " + mirrorClass, e);
    }
  }

  /**
   * Maps static members of the target mirror class.
   *
   * @param mirrorClass mirror class.
   * @throws MirrorException otherwise.
   */
  public static void map(Class<? extends MirrorClass> mirrorClass) throws MirrorException {
    map(null, mirrorClass);
  }
}
