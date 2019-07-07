package io.l0neman.utils.stay.reflect;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by l0neman on 2019/06/23.
 */
public class Reflect {

  private Class<?> mClass;
  private Object mObject;
  private static ThreadLocal<Reflect> sThreadState =
      new ThreadLocal<Reflect>() {
        @Override protected Reflect initialValue() {
          return new Reflect();
        }
      };

  public static Reflect with(String providerClass) {
    try {
      return with(Compat.classForName(providerClass));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Reflect with(Object targetObject) {
    if (targetObject instanceof Class) {
      return with((Class<?>) targetObject);
    }

    final Reflect reflectNew = sThreadState.get();
    // noinspection ConstantConditions [ensure not null].
    reflectNew.mClass = targetObject.getClass();
    reflectNew.mObject = targetObject;
    return reflectNew;
  }

  public static Reflect with(Class<?> providerClass) {
    final Reflect reflectNew = sThreadState.get();
    // noinspection ConstantConditions [ensure not null].
    reflectNew.mClass = providerClass;
    reflectNew.mObject = null;
    return reflectNew;
  }

  public Injector injector() {
    return new Injector();
  }

  public Invoker invoker() {
    return new Invoker();
  }

  public Builder builder() {
    return new Builder();
  }

  public Class<?> getClazz() {
    return mClass;
  }

  public final class Builder {
    private Class<?>[] constructorParameterTypes;

    public <T> T build() throws Exception {
      try {
        // noinspection unchecked
        return (T) getClazz().newInstance();
      } catch (Exception e) {
        throw new Exception("build", e);
      }
    }

    public void parameterTypes(Class<?>[] constructorParameterTypes) {
      this.constructorParameterTypes = constructorParameterTypes;
    }

    public <T> T build(Object... params) throws Exception {
      try {
        final Constructor<?> constructor =
            Compat.classFGetConstructor(getClazz(), constructorParameterTypes);

        // noinspection unchecked
        return (T) constructor.newInstance(params);
      } catch (Exception e) {
        throw new Exception("build", e);
      }
    }
  }

  public final class Injector {
    private String fieldName;

    public Injector field(String name) {
      this.fieldName = name;
      return this;
    }

    public void set(Object value) throws Exception {
      Field field = getDeclaredFieldFromClassTree(mClass);
      field.setAccessible(true);

      field.set(mObject, value);
    }

    public <T> T get() throws Exception {
      Field field = getDeclaredFieldFromClassTree(mClass);
      field.setAccessible(true);

      //noinspection unchecked - throw cast exception.
      return (T) field.get(mObject);
    }

    private Field getDeclaredFieldFromClassTree(Class<?> clazz) throws Exception {
      try {
        return Compat.classGetDeclaredField(clazz, fieldName);
      } catch (NoSuchFieldException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
          return getDeclaredFieldFromClassTree(parent);
        }
      }

      throw new Exception("not found field: " + fieldName + " from class: " + clazz);
    }

  }

  public final class Invoker {
    private String methodName;
    private Class<?>[] paramsTypes;

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
      Method targetMethod = getDeclaredMethodFromClassTree(mClass);
      targetMethod.setAccessible(true);

      //noinspection unchecked - throw cast exception.
      return (T) targetMethod.invoke(mObject, params);
    }

    private Method getDeclaredMethodFromClassTree(Class<?> clazz) throws Exception {
      try {
        return Compat.classGetDeclaredMethod(clazz, methodName, paramsTypes);
      } catch (NoSuchMethodException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
          return getDeclaredMethodFromClassTree(parent);
        }
      }

      throw new Exception("not found method: " + methodName + " from class: " + clazz);
    }
  }

  // 兼容 android P 禁止调用私有 api（双重反射）。
  private static final class Compat {
    private static Method sClassGetDeclaredField;
    private static Method sClassGetDeclaredMethod;
    private static Method sClassForName;
    private static Method sClassGetConstructor;

    private static final boolean NEED_COMPAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    static {
      try {
        sClassGetDeclaredField = Class.class.getMethod("getDeclaredField", String.class);
        sClassGetDeclaredMethod = Class.class.getMethod("getDeclaredMethod", String.class, Class[].class);
        sClassForName = Class.class.getMethod("forName", String.class);
        sClassGetConstructor = Class.class.getMethod("getConstructor", Class[].class);

      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    private static <T> Constructor<T> classFGetConstructor(Class<T> clazz,
                                                           Class<?>[] parameterTypes) throws Exception {
      // noinspection unchecked
      return NEED_COMPAT ? (Constructor<T>) sClassGetConstructor.invoke(clazz, (Object[]) parameterTypes) :
          clazz.getConstructor(parameterTypes);
    }

    private static Class<?> classForName(String classNam) throws Exception {
      return NEED_COMPAT ? (Class<?>) sClassForName.invoke(null, classNam) :
          Class.forName(classNam);
    }

    private static Field classGetDeclaredField(Class<?> clazz, String fieldName) throws Exception {
      return NEED_COMPAT ? (Field) sClassGetDeclaredField.invoke(clazz, fieldName) :
          clazz.getDeclaredField(fieldName);
    }

    private static Method classGetDeclaredMethod(Class<?> clazz, String fieldName,
                                                 Class<?>[] parameterTypes) throws Exception {
      return NEED_COMPAT ? (Method) sClassGetDeclaredMethod.invoke(clazz, fieldName, parameterTypes) :
          clazz.getDeclaredMethod(fieldName, parameterTypes);
    }
  }
}
