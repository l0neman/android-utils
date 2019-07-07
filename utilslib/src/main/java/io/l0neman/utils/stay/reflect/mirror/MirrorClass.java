package io.l0neman.utils.stay.reflect.mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.l0neman.utils.stay.reflect.Reflect;
import io.l0neman.utils.stay.reflect.mirror.annoation.MirrorClassName;
import io.l0neman.utils.stay.reflect.mirror.annoation.MirrorMethodParameterTypes;
import io.l0neman.utils.stay.reflect.mirror.throwable.MirrorException;

/**
 * Created by l0neman on 2019/07/06.
 */
public abstract class MirrorClass {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // reflect access.
  // key (method signature) - value (method info).
  private static Map<Class<?>, Map<String, MirrorMethod>> sMirrorClassMethodInfo = new HashMap<>();

  // MirrorMethod(Class<?> mClass, Object mObject, String mName, Class<?>[] mParameterTypes)
  private static Constructor<MirrorMethod> sMirrorMethodConstructorForInstance;
  // MirrorMethod(Class<?> mClass, String mName, Class<?>[] mParameterTypes)
  private static Constructor<MirrorMethod> sMirrorMethodConstructorForClass;

  static {
    try {
      sMirrorMethodConstructorForInstance = MirrorMethod.class.getConstructor(
          Class.class, Object.class, String.class, Class[].class);
      sMirrorMethodConstructorForClass = MirrorMethod.class.getConstructor(
          Class.class, String.class, Class[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  // MirrorField(Class<?> mClass, Object mObject, String mName)
  private static Map<Class<? extends MirrorField>, Constructor<? extends MirrorField>>
      sMirrorFieldConstructorForInstanceMap = new HashMap<>();

  // MirrorField(Class<?> mClass, String mName)
  private static Map<Class<? extends MirrorField>, Constructor<? extends MirrorField>>
      sMirrorFieldConstructorForClassMap = new HashMap<>();

  static {
    try {
      sMirrorFieldConstructorForInstanceMap.put(MirrorField.class,
          MirrorField.class.getConstructor(Class.class, Object.class, String.class));

      sMirrorFieldConstructorForClassMap.put(MirrorField.class,
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
      return Reflect.with(mirrorMethod.mObject).invoker()
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
    try {
      T mirrorObject = mirrorClass.newInstance();

      Class<?> targetMirrorClass = getTargetMirrorClass(mirrorClass);

      // init reflect fields.
      for (Field field : mirrorClass.getDeclaredFields()) {
        final Class<?> fieldType = field.getType();

        if (Modifier.isStatic(field.getModifiers())) {
          // for MirrorClass.
          if (MirrorClass.class.isAssignableFrom(fieldType)) {
            field.set(mirrorObject, MirrorClass.mirror(
                Reflect.with(targetMirrorClass).injector().field(field.getName()).get(),
                (Class<? extends MirrorClass>) fieldType
            ));

            continue; // end mirror class.
          }

          // for MirrorField.
          if (MirrorField.class.isAssignableFrom(fieldType)) {
            final Constructor<? extends MirrorField> fieldConstructor =
                sMirrorFieldConstructorForClassMap.get(fieldType);
            // noinspection ConstantConditions ensure no null.
            field.set(mirrorObject, fieldConstructor.newInstance(targetMirrorClass, field.getName()));

            continue; // end mirror field.
          }

          // for MirrorMethod.
          field.set(mirrorObject, sMirrorMethodConstructorForClass.newInstance(
              targetMirrorClass, field.getName(), getTargetMirrorMethodParameterTypes(field)));

          continue; // end static member.
        }

        // for MirrorClass.
        if (MirrorClass.class.isAssignableFrom(fieldType)) {

          field.set(mirrorObject, MirrorClass.mirror(
              Reflect.with(targetMirrorObject).injector().
                  field(field.getName()).get(),
              (Class<? extends MirrorClass>) fieldType
          ));

          continue; // end mirror class.
        }

        if (MirrorField.class.isAssignableFrom(fieldType)) {
          // for MirrorField.
          final Constructor<? extends MirrorField> constructor =
              sMirrorFieldConstructorForInstanceMap.get(fieldType);
          // noinspection ConstantConditions ensure no null.
          field.set(mirrorObject, constructor.newInstance(targetMirrorClass, targetMirrorObject,
              field.getName()));

          continue; // end mirror field.
        }

        // for MirrorMethod.
        field.set(mirrorObject, sMirrorMethodConstructorForClass.newInstance(
            targetMirrorClass, field.getName(), getTargetMirrorMethodParameterTypes(field)));
      }

      // init reflect methods.
      if (sMirrorClassMethodInfo.containsKey(mirrorClass)) {
        return mirrorObject;
      }

      // Record mirror method info for another way of mapping
      Map<String, MirrorMethod> reflectMethodMap = new HashMap<>();
      for (Method method : mirrorClass.getDeclaredMethods()) {
        String methodSignature = getMethodSignature(method.getName(), method.getParameterTypes());

        if (Modifier.isStatic(method.getModifiers())) {
          reflectMethodMap.put(methodSignature, sMirrorMethodConstructorForClass.newInstance(
              targetMirrorClass, method.getName(), method.getParameterTypes()
          ));

          continue;
        }

        reflectMethodMap.put(methodSignature, sMirrorMethodConstructorForInstance.newInstance(
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
