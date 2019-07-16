package io.l0neman.utils.stay.reflect.mirror;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.annoation.MirrorClassName;
import io.l0neman.utils.stay.reflect.mirror.annoation.MirrorMethodParameterTypes;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;
import io.l0neman.utils.stay.reflect.mirror.util.MirrorClassesInfoCache;

/**
 * Created by l0neman on 2019/07/06.
 */
public abstract class MirrorClass {

  private static MirrorClassesInfoCache sReflectClassesInfoCache = new MirrorClassesInfoCache();
  Object mTargetMirrorObject;

  // connect method parameterTypes class name.
  private static String getMethodSignature(String name, Class<?>[] parameterTypes) {
    StringBuilder sb = new StringBuilder();
    for (Class<?> pt : parameterTypes) {
      sb.append(pt.getSimpleName());
    }

    return name + sb.toString();
  }

  /**
   * You need to call this method to provide method information when you need to
   * map an instance method of a target mirror class.
   *
   * @param name           fun name.
   * @param parameterTypes fun parameterTypes.
   * @param args           fun pass params.
   * @return The return value of the target mapping method
   */
  protected Object invoke(String name, Class<?>[] parameterTypes, Object... args) {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    final Class<? extends MirrorClass> clazz = this.getClass();
    final MirrorClassesInfoCache.ReflectClassInfo reflectClassInfo =
        sReflectClassesInfoCache.getReflectClassInfo(clazz);

    String methodSignature = getMethodSignature(name, parameterTypes);
    final Method method = reflectClassInfo.queryMethod(methodSignature);

    try {
      return Reflect.with(method).targetObject(mTargetMirrorObject).invoke(args);
    } catch (Reflect.ReflectException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * You need to call this method to provide method information when you need to
   * map a static method of the target mirror class.
   *
   * @param mirrorClass    mirror class
   * @param name           fun name.
   * @param parameterTypes fun parameterTypes.
   * @param args           fun pass params.
   * @return The return value of the target mapping method
   */
  protected static Object invokeStatic(Class<?> mirrorClass, String name,
                                       Class<?>[] parameterTypes, Object... args) {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    final MirrorClassesInfoCache.ReflectClassInfo reflectClassInfo =
        sReflectClassesInfoCache.getReflectClassInfo(mirrorClass);
    String methodSignature = getMethodSignature(name, parameterTypes);
    final Method method = reflectClassInfo.queryMethod(methodSignature);

    try {
      return Reflect.with(method).invoke(args);
    } catch (Reflect.ReflectException e) {
      throw new RuntimeException(e);
    }
  }

  private static Class<?> getTargetMirrorClass(Class<? extends MirrorClass> mirrorClass)
      throws MirrorException {
    final MirrorClassName mirrorClassName = mirrorClass.getAnnotation(MirrorClassName.class);
    if (mirrorClassName == null) {
      throw new MirrorException("not get mirror class name, please add MirrorClassName annotation. " +
          "mirror class: " + mirrorClass.getSimpleName());
    }

    return Reflect.with(mirrorClassName.value()).getClazz();
  }

  private static Class<?>[] getTargetMirrorMethodParameterTypes(Field mirrorMethod)
      throws MirrorException {
    final MirrorMethodParameterTypes mirrorMethodParameterTypes =
        mirrorMethod.getAnnotation(MirrorMethodParameterTypes.class);
    if (mirrorMethodParameterTypes == null) {
      throw new MirrorException("not get mirror method parameter types, " +
          "please add MirrorMethodParameterTypes annotation. mirror method: " +
          mirrorMethod.getName());
    }

    return mirrorMethodParameterTypes.value();
  }

  /**
   * Map static members and instance members of a target mirror class.
   * <p>
   * (build mirror class info and set mirror class's field and method).
   *
   * @param targetMirrorObject target mirror class instance.
   * @param mirrorClass        mirror class.
   * @param <T>                subclass of MirrorClass
   * @return new mirror class instance that completes the mapping.
   *
   * @throws MirrorException otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T extends MirrorClass> T mirror(Object targetMirrorObject, Class<T> mirrorClass)
      throws MirrorException {

    MirrorClassesInfoCache.ReflectClassInfo reflectClassInfo =
        sReflectClassesInfoCache.getReflectClassInfo(mirrorClass);

    MirrorClassesInfoCache.ReflectClassInfo newReflectClassInfo = null;

    if (reflectClassInfo == null) {
      newReflectClassInfo = new MirrorClassesInfoCache.ReflectClassInfo();
      sReflectClassesInfoCache.saveReflectClassInfo(mirrorClass, newReflectClassInfo);
    }

    try {
      T mirrorObject = mirrorClass.newInstance();
      mirrorObject.mTargetMirrorObject = targetMirrorObject;

      Class<?> targetMirrorClass = getTargetMirrorClass(mirrorClass);

      for (Field field : mirrorClass.getDeclaredFields()) {
        final Class<?> fieldType = field.getType();

        // for MirrorClass.
        if (MirrorClass.class.isAssignableFrom(fieldType)) {
          field.set(mirrorObject, MirrorClass.mirror(
              Reflect.with(targetMirrorClass).injector().field(field.getName()).get(),
              (Class<? extends MirrorClass>) fieldType
          ));

          continue; // end mirror class.
        }

        // for mirror field.
        if (MirrorField.class.isAssignableFrom(fieldType)) {
          Field targetMirrorField;

          if (reflectClassInfo == null) {
            targetMirrorField = Reflect.with(targetMirrorClass).injector().field(field.getName())
                .getField();

            newReflectClassInfo.saveField(field.getName(), targetMirrorField);
          } else {
            targetMirrorField = reflectClassInfo.queryField(field.getName());
          }

          field.set(mirrorObject, new MirrorField(targetMirrorObject, targetMirrorField));

          continue; // end mirror field.
        }

        // for mirror method.
        if (MirrorMethod.class.isAssignableFrom(fieldType)) {
          Method targetMirrorMethod;
          final Class<?>[] targetMirrorMethodParameterTypes = getTargetMirrorMethodParameterTypes(field);
          String methodSignature = getMethodSignature(field.getName(), targetMirrorMethodParameterTypes);

          if (reflectClassInfo == null) {
            targetMirrorMethod = Reflect.with(targetMirrorClass).invoker().method(field.getName())
                .paramsType(targetMirrorMethodParameterTypes).getMethod();

            newReflectClassInfo.saveMethod(methodSignature, targetMirrorMethod);
          } else {
            targetMirrorMethod = reflectClassInfo.queryMethod(methodSignature);
          }

          field.set(mirrorObject, new MirrorMethod(targetMirrorObject, targetMirrorMethod));

          // end mirror method.
        }
      }

      return mirrorObject;

    } catch (Exception e) {
      throw new MirrorException("mirror", e);
    }
  }

  /**
   * Map only the target mirror object to a static member
   *
   * @param mirrorClass mirror class.
   * @throws MirrorException otherwise.
   */
  public static void mirror(Class<? extends MirrorClass> mirrorClass) throws MirrorException {
    mirror(null, mirrorClass);
  }
}
