package io.l0neman.utils.stay.reflect;

import io.l0neman.utils.stay.reflect.annoation.MirrorClassName;
import io.l0neman.utils.stay.reflect.throwable.ReflectException;
import io.l0neman.utils.stay.reflect.util.Reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by l0neman on 2019/07/06.
 */
@SuppressWarnings("ALL")
public abstract class ReflectClass {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // reflect access.
  // key (method signature) - value (method info).
  static Map<Class<?>, Map<String, ReflectMethod>> sMirrorClassMethodInfo = new HashMap<>();

  // ReflectMethod(Class<?> mClass, Object mObject, String mName, Class<?>[] mParameterTypes)
  private static Constructor<ReflectMethod> sReflectMethodConstructorForInstance;
  // ReflectMethod(Class<?> mClass, String mName, Class<?>[] mParameterTypes)
  private static Constructor<ReflectMethod> sReflectMethodConstructorForClass;

  static {
    try {
      sReflectMethodConstructorForInstance = ReflectMethod.class.getConstructor(Class.class, Object.class, String.class, Class[].class);
      sReflectMethodConstructorForClass = ReflectMethod.class.getConstructor(Class.class, String.class, Class[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  // ReflectField(Class<?> mClass, Object mObject, String mName)
  private static Map<Class<? extends ReflectField>, Constructor<? extends ReflectField>>
      sReflectFieldConstructorForInstanceMap = new HashMap<>();

  // ReflectField(Class<?> mClass, String mName)
  private static Map<Class<? extends ReflectField>, Constructor<? extends ReflectField>>
      sReflectFieldConstructorForClassMap = new HashMap<>();

  static {
    try {
      sReflectFieldConstructorForInstanceMap.put(ReflectField.class, ReflectField.class.getConstructor(Class.class, Object.class, String.class));
      sReflectFieldConstructorForInstanceMap.put(ObjectReflectField.class, ObjectReflectField.class.getConstructor(Class.class, Object.class, String.class));
      sReflectFieldConstructorForInstanceMap.put(IntReflectField.class, IntReflectField.class.getConstructor(Class.class, Object.class, String.class));

      sReflectFieldConstructorForClassMap.put(ReflectField.class, ReflectField.class.getConstructor(Class.class, String.class));
      sReflectFieldConstructorForClassMap.put(ObjectReflectField.class, ObjectReflectField.class.getConstructor(Class.class, String.class));
      sReflectFieldConstructorForClassMap.put(IntReflectField.class, IntReflectField.class.getConstructor(Class.class, String.class));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  // connect method parameterTypes class name.
  private static String getMethodSignature(String name, Class<?>[] parameterTypes) {
    StringBuilder sb = new StringBuilder();
    for (Class<?> pt : parameterTypes) {
      sb.append(pt.getSimpleName());
    }

    return name + sb.toString();
  }

  protected Object invoke(String name, Class<?>[] parameterTypes, Object... args) {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    final Class<? extends ReflectClass> aClass = this.getClass();
    ReflectMethod reflectMethod = sMirrorClassMethodInfo.get(aClass)
        .get(getMethodSignature(name, parameterTypes));

    try {
      return invoke(reflectMethod, args);
    } catch (ReflectException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Object invokeStatic(Class<?> reflectClass, String name, Class<?>[] parameterTypes, Object... args) {
    if (parameterTypes == null) {
      parameterTypes = new Class[0];
    }

    ReflectMethod reflectMethod = sMirrorClassMethodInfo.get(reflectClass)
        .get(getMethodSignature(name, parameterTypes));

    try {
      return invoke(reflectMethod, args);
    } catch (ReflectException e) {
      throw new RuntimeException(e);
    }
  }

  // The mObject of the static method is null.
  private static Object invoke(ReflectMethod reflectMethod, Object... args) throws ReflectException {
    try {
      return Reflect.with(reflectMethod.mClass).invoker()
          .targetObject(reflectMethod.mObject)
          .method(reflectMethod.mName)
          .paramsType(reflectMethod.mParameterTypes)
          .invoke(args);
    } catch (Exception e) {
      throw new ReflectException("call method", e);
    }
  }

  private static Class<?> getTargetMirrorClass(Class<? extends ReflectClass> mirrorClass) throws ReflectException {
    final MirrorClassName mirrorClassName = mirrorClass.getAnnotation(MirrorClassName.class);
    if (mirrorClassName == null) {
      throw new ReflectException("not get mirror class name");
    }

    return Reflect.with(mirrorClassName.value()).getProviderClass();
  }

  // init instance members and static members.
  public static <T extends ReflectClass> T mirror(Object targetMirrorObject, Class<T> mirrorClass)
      throws ReflectException {
    try {
      T mirrorObject = mirrorClass.newInstance();

      Class<?> targetMirrorClass = getTargetMirrorClass(mirrorClass);

      // init reflect fields.
      for (Field field : mirrorClass.getDeclaredFields()) {

        if (Modifier.isStatic(field.getModifiers())) {
          // for ReflectClass.
          if (ReflectClass.class.isAssignableFrom(field.getType())) {

            field.set(mirrorObject, ReflectClass.mirror(
                Reflect.with(targetMirrorClass).injector().field(field.getName()).get(),
                (Class<? extends ReflectClass>) field.getType()
            ));

            continue;
          }

          // for ReflectField.
          final Constructor<? extends ReflectField> constructor = sReflectFieldConstructorForClassMap.get(field.getType());
          field.set(mirrorObject, constructor.newInstance(targetMirrorClass, field.getName()));

          continue;
        }

        // for ReflectClass.
        if (ReflectClass.class.isAssignableFrom(field.getType())) {
          final Class<?> fieldTargetMirrorClass = getTargetMirrorClass((Class<? extends ReflectClass>) field.getType());

          field.set(mirrorObject, ReflectClass.mirror(
              Reflect.with(targetMirrorClass).injector().targetObject(targetMirrorObject).field(field.getName()).get(),
              (Class<? extends ReflectClass>) field.getType()
          ));

          continue;
        }

        // for ReflectField.
        final Constructor<? extends ReflectField> constructor = sReflectFieldConstructorForInstanceMap.get(field.getType());
        field.set(mirrorObject, constructor.newInstance(targetMirrorClass, targetMirrorObject, field.getName()));
      }

      // init reflect methods.
      if (sMirrorClassMethodInfo.containsKey(mirrorClass)) {
        return mirrorObject;
      }

      Map<String, ReflectMethod> reflectMethodMap = new HashMap<>();
      for (Method method : mirrorClass.getDeclaredMethods()) {
        String methodSignature = getMethodSignature(method.getName(), method.getParameterTypes());

        if (Modifier.isStatic(method.getModifiers())) {
          reflectMethodMap.put(methodSignature, sReflectMethodConstructorForClass.newInstance(
              targetMirrorClass, method.getName(), method.getParameterTypes()
          ));

          continue;
        }

        reflectMethodMap.put(methodSignature, sReflectMethodConstructorForInstance.newInstance(
            targetMirrorClass, targetMirrorObject, method.getName(), method.getParameterTypes()
        ));
      }

      sMirrorClassMethodInfo.put(mirrorClass, reflectMethodMap);
      return mirrorObject;
    } catch (Exception e) {
      throw new ReflectException("mirror", e);
    }
  }

  // mirror static members.
  public static void mirrorStaticMembers(Class<? extends ReflectClass> mirrorClass) throws ReflectException {
    mirror(null, mirrorClass);
  }
}
