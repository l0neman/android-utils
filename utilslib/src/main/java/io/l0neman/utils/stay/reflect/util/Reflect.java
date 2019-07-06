package io.l0neman.utils.stay.reflect.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by l0neman on 2019/06/23.
 */
public class Reflect {

  private Class<?> providerClass;
  private Object targetObject;
  private static ThreadLocal<Reflect> sThreadState =
      new ThreadLocal<Reflect>() {
        @Override
        protected Reflect initialValue() {
          return new Reflect();
        }
      };

  public static Reflect with(String providerClass) {
    try {
      return with(Class.forName(providerClass));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Reflect with(Object targetObject) {
    final Reflect reflectNew = sThreadState.get();
    reflectNew.providerClass = targetObject.getClass();
    reflectNew.targetObject = targetObject;
    return reflectNew;
  }

  public static Reflect with(Class<?> providerClass) {
    final Reflect reflectNew = sThreadState.get();
    reflectNew.providerClass = providerClass;
    reflectNew.targetObject = null;
    return reflectNew;
  }

  public Injector injector() {
    return new Injector();
  }

  public Invoker invoker() {
    return new Invoker();
  }

  public Class<?> getProviderClass() {
    return providerClass;
  }

  public abstract class ReflectUtils {
    public ReflectUtils providerClass(String providerClass) {
      try {
        return providerClass(Class.forName(providerClass));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public ReflectUtils providerClass(Class<?> providerClass) {
      Reflect.this.providerClass = providerClass;
      return this;
    }

    public ReflectUtils targetObject(Object targetObject) {
      Reflect.this.targetObject = targetObject;
      return this;
    }
  }

  public final class Injector extends ReflectUtils {
    private String fieldName;

    public Injector field(String name) {
      this.fieldName = name;
      return this;
    }

    public Injector providerClass(String providerClass) {
      super.providerClass(providerClass);
      return this;
    }

    public Injector providerClass(Class<?> providerClass) {
      super.providerClass(providerClass);
      return this;
    }

    public Injector targetObject(Object targetObject) {
      super.targetObject(targetObject);
      return this;
    }

    public void set(Object value) throws Exception {
      Field field = providerClass.getDeclaredField(fieldName);
      field.setAccessible(true);

      field.set(targetObject, value);
    }

    public <T> T get() throws Exception {
      Field field = providerClass.getDeclaredField(fieldName);
      field.setAccessible(true);

      //noinspection unchecked - throw cast exception.
      return (T) field.get(targetObject);
    }

  }

  public final class Invoker extends ReflectUtils {
    private String methodName;
    private Class<?>[] paramsTypes;

    public Invoker providerClass(String providerClass) {
      super.providerClass(providerClass);
      return this;
    }

    public Invoker providerClass(Class<?> providerClass) {
      super.providerClass(providerClass);
      return this;
    }

    public Invoker targetObject(Object targetObject) {
      super.targetObject(targetObject);
      return this;
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

    public <T> T invoke(Object... params) throws Exception {
      Method targetMethod = providerClass.getDeclaredMethod(methodName, paramsTypes);
      targetMethod.setAccessible(true);

      //noinspection unchecked - throw cast exception.
      return (T) targetMethod.invoke(targetObject, params);
    }
  }
}
