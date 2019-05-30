package com.runing.utilslib.general.sugar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection utils.
 * Created by runing on 2018/11/28.
 */
public final class ReflectUtils {

  private Class<?> clazz;
  private Object target;
  private Injector injector = new Injector();
  private Invoker invoker = new Invoker();
  private static ThreadLocal<ReflectUtils> sUtilsInstance;

  private ReflectUtils() {}

  /**
   * clean an object.
   */
  public static void recycle() {
    sUtilsInstance = null;
  }

  /* 缓存工具对象，不使用常量。 */
  private static ReflectUtils apply() {
    if (sUtilsInstance == null) {
      sUtilsInstance = new ThreadLocal<>();
    }

    ReflectUtils reflectUtils = sUtilsInstance.get();
    if (reflectUtils == null) {
      reflectUtils = new ReflectUtils();
      return reflectUtils;
    }

    reflectUtils.clear();
    reflectUtils.invoker.clear();
    reflectUtils.injector.clear();
    return reflectUtils;
  }

  private void clear() {
    clazz = null;
    target = null;
  }

  /**
   * Gets the {@link Injector} instance from target object.
   *
   * @param target Target object
   */
  public static Injector inject(Object target) {
    ReflectUtils apply = apply();
    apply.target = target;
    return apply.injector;
  }

  /**
   * Gets the {@link Injector} instance from target Class.
   *
   * @param clazz Target class
   */
  public static Injector inject(Class<?> clazz) {
    ReflectUtils apply = apply();
    apply.clazz = clazz;
    return apply.injector;
  }


  /**
   * Gets the {@link Invoker} instance from target object.
   *
   * @param target Target object
   */
  public static Invoker invoke(Object target) {
    ReflectUtils apply = apply();
    apply.target = target;
    return apply.invoker;
  }

  /**
   * Gets the {@link Invoker} instance from target clazz.
   *
   * @param clazz Target clazz
   */
  public static Invoker invoke(Class<?> clazz) {
    ReflectUtils apply = apply();
    apply.clazz = clazz;
    return apply.invoker;
  }

  private static Class<?> getSuperClassByLevel(Class<?> clazz, final int level) {
    Class<?> targetClass = clazz;
    for (int i = 0; i < level; i++) {
      if (targetClass != null) {
        targetClass = targetClass.getSuperclass();
      }
    }

    return targetClass;
  }

  private static Class<?> getSuperClassByLevel(Object target, final int level) {
    return getSuperClassByLevel(target.getClass(), level);
  }

  /**
   * utils base class
   */
  private class Reflector {
    int superLevel;

    Reflector superLevel(int level) {
      this.superLevel = level;
      return this;
    }

    Class<?> getTargetClass() {
      return target != null ?
          getSuperClassByLevel(target, superLevel) :
          getSuperClassByLevel(clazz, superLevel);
    }
  }

  /**
   * Operate on the target field
   */
  public class Injector extends Reflector {
    private String fieldName;

    public Injector field(String name) {
      this.fieldName = name;
      return this;
    }

    void clear() {
      fieldName = null;
      superLevel = 0;
    }

    /**
     * Sets the superclass level.
     *
     * @param level superclass level
     * @return self
     */
    @Override
    public Injector superLevel(int level) {
      return (Injector) super.superLevel(level);
    }

    /**
     * see {@linkplain #setX(Object)}
     *
     * @throws Exception {@link NoSuchFieldException} or {@link IllegalAccessException}
     */
    public void set(Object value) throws Exception {
      setX(value);
    }

    /**
     * Sets the value for the target field.
     *
     * @param value value
     * @throws ReflectiveOperationException Does not support Android API 19 below.
     *                                      {@link NoSuchFieldException} or
     *                                      {@link IllegalAccessException}
     */
    public void setX(Object value) throws ReflectiveOperationException {
      final Class<?> targetClass = getTargetClass();

      Field field = targetClass.getDeclaredField(fieldName);
      field.setAccessible(true);

      if (target != null) {
        field.set(target, value);
      } else {
        field.set(null, value);
      }
    }

    /**
     * see {@linkplain #getX()}
     *
     * @throws Exception {@link NoSuchFieldException} or {@link IllegalAccessException}
     */
    public Object get() throws Exception {
      return getX();
    }

    /**
     * Gets the value of the target.
     *
     * @return field value
     *
     * @throws ReflectiveOperationException {@link NoSuchFieldException} or
     *                                      {@link IllegalAccessException}
     */
    public Object getX() throws ReflectiveOperationException {
      Class<?> targetClass = getTargetClass();

      Field field = targetClass.getDeclaredField(fieldName);
      field.setAccessible(true);

      return target != null ?
          field.get(target) :
          field.get(null);
    }
  }

  /**
   * Operate on the target method
   */
  public class Invoker extends Reflector {
    private String methodName;
    private Class<?>[] paramsTypes;

    @Override
    Invoker superLevel(int level) {
      return (Invoker) super.superLevel(level);
    }

    void clear() {
      methodName = null;
      paramsTypes = null;
      superLevel = 0;
    }

    /**
     * Set the method name.
     *
     * @param methodName method"s name
     * @return self
     */
    public Invoker method(String methodName) {
      this.methodName = methodName;
      return this;
    }

    /**
     * Sets the type of the method's arguments.
     *
     * @param paramsTypes methods params types.
     * @return self
     */
    public Invoker paramsType(Class<?>... paramsTypes) {
      this.paramsTypes = paramsTypes;
      return this;
    }

    /**
     * see {@linkplain #invokeX(Object...)}
     *
     * @param params method's params
     * @return method result
     *
     * @throws Exception {@link NoSuchFieldException} or {@link IllegalAccessException} or
     *                   {@link InvocationTargetException}
     */
    public Object invoke(Object... params) throws Exception {
      final Class<?> targetClass = getTargetClass();

      Method targetMethod = targetClass.getDeclaredMethod(methodName, paramsTypes);
      targetMethod.setAccessible(true);

      return target != null ?
          targetMethod.invoke(target, params) :
          targetMethod.invoke(null, params);
    }

    /**
     * Call the target method.
     *
     * @param params method's params
     * @return method result
     *
     * @throws ReflectiveOperationException {@link NoSuchFieldException} or {@link IllegalAccessException} or
     *                                      {@link InvocationTargetException}
     */
    public Object invokeX(Object... params) throws ReflectiveOperationException {
      final Class<?> targetClass = getTargetClass();

      Method targetMethod = targetClass.getDeclaredMethod(methodName, paramsTypes);
      targetMethod.setAccessible(true);

      return target != null ?
          targetMethod.invoke(target, params) :
          targetMethod.invoke(null, params);
    }
  }
}
