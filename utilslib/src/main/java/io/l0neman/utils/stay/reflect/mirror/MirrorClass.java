package io.l0neman.utils.stay.reflect.mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.l0neman.utils.stay.reflect.mirror.annoation.MirrorClassName;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;
import io.l0neman.utils.stay.reflect.util.Reflect;

/**
 * Created by l0neman on 2019/07/06.
 */
public abstract class MirrorClass {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // reflect access.
  // key (method signature) - value (method info).
  private static Map<Class<?>, Map<String, MirrorMethod>> sMirrorClassMethodInfo = new HashMap<>();

  // ReflectMethod(Class<?> mClass, Object mObject, String mName, Class<?>[] mParameterTypes)
  private static Constructor<MirrorMethod> sReflectMethodConstructorForInstance;
  // ReflectMethod(Class<?> mClass, String mName, Class<?>[] mParameterTypes)
  private static Constructor<MirrorMethod> sReflectMethodConstructorForClass;

  static {
    try {
      sReflectMethodConstructorForInstance = MirrorMethod.class.getConstructor(
          Class.class, Object.class, String.class, Class[].class);
      sReflectMethodConstructorForClass = MirrorMethod.class.getConstructor(
          Class.class, String.class, Class[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  // ReflectField(Class<?> mClass, Object mObject, String mName)
  private static Map<Class<? extends MirrorField>, Constructor<? extends MirrorField>>
      sReflectFieldConstructorForInstanceMap = new HashMap<>();

  // ReflectField(Class<?> mClass, String mName)
  private static Map<Class<? extends MirrorField>, Constructor<? extends MirrorField>>
      sReflectFieldConstructorForClassMap = new HashMap<>();

  static {
    try {
      sReflectFieldConstructorForInstanceMap.put(MirrorField.class,
          MirrorField.class.getConstructor(Class.class, Object.class, String.class));

      sReflectFieldConstructorForClassMap.put(MirrorField.class,
          MirrorField.class.getConstructor(Class.class, String.class));
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

    final Class<? extends MirrorClass> aClass = this.getClass();
    // noinspection ConstantConditions ensure not null.
    MirrorMethod reflectMethod = sMirrorClassMethodInfo.get(aClass)
        .get(getMethodSignature(name, parameterTypes));

    try {
      return invoke(reflectMethod, args);
    } catch (MirrorException e) {
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

    // noinspection ConstantConditions // ensure not null.
    MirrorMethod reflectMethod = sMirrorClassMethodInfo.get(mirrorClass)
        .get(getMethodSignature(name, parameterTypes));

    try {
      return invoke(reflectMethod, args);
    } catch (MirrorException e) {
      throw new RuntimeException(e);
    }
  }

  // The mObject of the static method is null.
  private static Object invoke(MirrorMethod mirrorMethod, Object... args) throws MirrorException {
    try {
      return Reflect.with(mirrorMethod.mClass).invoker()
          .targetObject(mirrorMethod.mObject)
          .method(mirrorMethod.mName)
          .paramsType(mirrorMethod.mParameterTypes)
          .invoke(args);
    } catch (Exception e) {
      throw new MirrorException("call method", e);
    }
  }

  private static Class<?> getTargetMirrorClass(Class<? extends MirrorClass> mirrorClass)
      throws MirrorException {
    final MirrorClassName mirrorClassName = mirrorClass.getAnnotation(MirrorClassName.class);
    if (mirrorClassName == null) {
      throw new MirrorException("not get mirror class name, please add MirrorClassName annotation.");
    }

    return Reflect.with(mirrorClassName.value()).getProviderClass();
  }

  /**
   * Map static members and instance members of a target mirror class.
   *
   * @param targetMirrorObject target mirror class instance.
   * @param mirrorClass        mirror class.
   * @param <T>                subclass of ReflectClass
   * @return new mirror class instance that completes the mapping.
   *
   * @throws MirrorException otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T extends MirrorClass> T mirror(Object targetMirrorObject, Class<T> mirrorClass)
      throws MirrorException {
    try {
      T mirrorObject = mirrorClass.newInstance();

      Class<?> targetMirrorClass = getTargetMirrorClass(mirrorClass);

      // init reflect fields.
      for (Field field : mirrorClass.getDeclaredFields()) {
        final Class<?> fieldType = field.getType();

        if (Modifier.isStatic(field.getModifiers())) {
          // for ReflectClass.
          if (MirrorClass.class.isAssignableFrom(fieldType)) {

            field.set(mirrorObject, MirrorClass.mirror(
                Reflect.with(targetMirrorClass).injector().field(field.getName()).get(),
                (Class<? extends MirrorClass>) fieldType
            ));

            continue;
          }

          // for ReflectField.
          final Constructor<? extends MirrorField> constructor =
              sReflectFieldConstructorForClassMap.get(fieldType);
          // noinspection ConstantConditions ensure no null.
          field.set(mirrorObject, constructor.newInstance(targetMirrorClass, field.getName()));

          continue;
        }

        // for ReflectClass.
        if (MirrorClass.class.isAssignableFrom(fieldType)) {

          field.set(mirrorObject, MirrorClass.mirror(
              Reflect.with(targetMirrorClass).injector().targetObject(targetMirrorObject).
                  field(field.getName()).get(),
              (Class<? extends MirrorClass>) fieldType
          ));

          continue;
        }

        // for ReflectField.
        final Constructor<? extends MirrorField> constructor =
            sReflectFieldConstructorForInstanceMap.get(fieldType);
        // noinspection ConstantConditions ensure no null.
        field.set(mirrorObject, constructor.newInstance(targetMirrorClass, targetMirrorObject,
            field.getName()));
      }

      // init reflect methods.
      if (sMirrorClassMethodInfo.containsKey(mirrorClass)) {
        return mirrorObject;
      }

      Map<String, MirrorMethod> reflectMethodMap = new HashMap<>();
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
