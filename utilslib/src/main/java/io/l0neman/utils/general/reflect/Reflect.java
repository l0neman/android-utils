package io.l0neman.utils.general.reflect;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by l0neman on 2019/06/23.
 */
public class Reflect {

  Class<?> mClass;
  Object mObject;
  private static ThreadLocal<Reflect> sThreadState =
      new ThreadLocal<Reflect>() {
        @Override protected Reflect initialValue() {
          return new Reflect();
        }
      };

  public static class ReflectException extends Exception {

    private Exception original;

    public ReflectException(String message, Throwable cause) {
      super(message, cause);
    }

    public Exception getOriginal() {
      return original;
    }
  }

  private void ensureClean() {
    if (mClass != null || mObject != null) {
      throw new UnsupportedOperationException("It is not allowed to use \"with\" after \"with\".");
    }
  }

  // clean temp
  private void clean() {
    mClass = null;
    mObject = null;
  }

  public static Reflect with(String className) {
    try {
      return with(Compat.classForName(className));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Reflect with(Object object) {
    final Reflect reflectNew = sThreadState.get();
    // noinspection ConstantConditions [ensure not null].
    reflectNew.ensureClean();
    reflectNew.mClass = object.getClass();
    reflectNew.mObject = object;
    return reflectNew;
  }

  public static Reflect with(Class<?> clazz) {
    final Reflect reflectNew = sThreadState.get();
    // noinspection ConstantConditions [ensure not null].
    reflectNew.mClass = clazz;
    return reflectNew;
  }

  public static Creator with(Constructor<?> constructor) {
    final Creator creator = new Creator();
    creator.constructor = constructor;
    return creator;
  }

  public static Injector with(Field field) {
    final Injector injector = new Injector();
    injector.mField = field;
    return injector;
  }

  public static Invoker with(Method method) {
    final Invoker invoker = new Invoker();
    invoker.mMethod = method;
    return invoker;
  }

  public Injector injector() {
    final Injector injector = new Injector();
    injector.mClass = mClass;
    injector.mObject = mObject;
    clean();
    return injector;
  }

  public Invoker invoker() {
    final Invoker invoker = new Invoker();
    invoker.mClass = mClass;
    invoker.mObject = mObject;
    clean();
    return invoker;
  }

  public Creator creator() {
    final Creator creator = new Creator();
    creator.mClass = mClass;
    creator.mObject = mObject;
    clean();
    return creator;
  }

  public final Class<?> getClazz() {
    clean();
    return mClass;
  }

  public final static class Creator extends Reflect {
    private Constructor<?> constructor;
    private Class<?>[] constructorParameterTypes;

    private Creator() {}

    public <T> T create() throws ReflectException {
      try {
        if (constructor != null) {
          // noinspection unchecked
          return (T) constructor.newInstance();
        }

        // noinspection unchecked
        return (T) getClazz().newInstance();
      } catch (Exception e) {
        throw new ReflectException("build", e);
      }
    }

    public Creator parameterTypes(Class<?>... constructorParameterTypes) {
      this.constructorParameterTypes = constructorParameterTypes;
      return this;
    }

    public <T> T create(Object... params) throws ReflectException {
      try {
        if (constructor != null) {
          // noinspection unchecked
          return (T) constructor.newInstance(params);
        }

        final Constructor<?> constructor =
            Compat.classFGetConstructor(getClazz(), constructorParameterTypes);

        // noinspection unchecked
        return (T) constructor.newInstance(params);
      } catch (Exception e) {
        throw new ReflectException("create", e);
      }
    }
  }

  public final static class Injector extends Reflect {
    private Field mField;
    private String mFieldName;

    private Injector() {}

    public Injector field(String name) {
      this.mFieldName = name;
      return this;
    }

    public Injector targetObject(Object object) {
      this.mObject = object;
      return this;
    }

    public void set(Object value) throws ReflectException {
      try {
        if (mField != null) {
          mField.set(mObject, value);
          return;
        }

        Field field = getDeclaredFieldFromClassTree(mClass);
        field.setAccessible(true);

        field.set(mObject, value);
      } catch (Exception e) {
        throw new ReflectException("injector set", e);
      }
    }

    public Field getField() throws ReflectException {
      try {
        if (mField != null) {
          return mField;
        }

        Field field = getDeclaredFieldFromClassTree(mClass);
        field.setAccessible(true);

        return field;
      } catch (Exception e) {
        throw new ReflectException("injector get", e);
      }
    }

    public <T> T get() throws ReflectException {
      try {
        if (mField != null) {
          // noinspection unchecked - throw cast exception.
          return (T) mField.get(mObject);
        }

        Field field = getDeclaredFieldFromClassTree(mClass);
        field.setAccessible(true);

        // noinspection unchecked - throw cast exception.
        return (T) field.get(mObject);
      } catch (Exception e) {
        throw new ReflectException("injector get", e);
      }
    }

    private Field getDeclaredFieldFromClassTree(Class<?> clazz) throws Exception {
      try {
        return Compat.classGetDeclaredField(clazz, mFieldName);
      } catch (NoSuchFieldException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
          return getDeclaredFieldFromClassTree(parent);
        }
      }

      throw new Exception("not found mField: " + mFieldName + " from class: " + clazz);
    }

  }

  public final static class Invoker extends Reflect {
    private Method mMethod;
    private String mMethodName;
    private Class<?>[] mParamsTypes;

    private Invoker() {}

    /**
     * Set the mMethod name.
     *
     * @param methodName mMethod"s name
     * @return self
     */
    public Invoker method(String methodName) {
      this.mMethodName = methodName;
      return this;
    }

    public Invoker targetObject(Object object) {
      this.mObject = object;
      return this;
    }

    /**
     * Sets the type of the mMethod's arguments.
     *
     * @param paramsTypes methods params types.
     * @return self
     */
    public Invoker paramsType(Class<?>... paramsTypes) {
      this.mParamsTypes = paramsTypes;
      return this;
    }

    public Method getMethod() throws ReflectException {
      try {
        if (mMethod != null) {
          return mMethod;
        }

        Method targetMethod = getDeclaredMethodFromClassTree(mClass);
        targetMethod.setAccessible(true);

        return targetMethod;
      } catch (Exception e) {
        throw new ReflectException("Invoker invoke", e);
      }
    }

    public <T> T invoke(Object... params) throws ReflectException {
      try {
        if (mMethod != null) {
          // noinspection unchecked - throw cast exception.
          return (T) mMethod.invoke(mObject, params);
        }

        Method targetMethod = getDeclaredMethodFromClassTree(mClass);
        targetMethod.setAccessible(true);

        // noinspection unchecked - throw cast exception.
        return (T) targetMethod.invoke(mObject, params);
      } catch (Exception e) {
        throw new ReflectException("Invoker invoke", e);
      }
    }

    private Method getDeclaredMethodFromClassTree(Class<?> clazz) throws Exception {
      try {
        return Compat.classGetDeclaredMethod(clazz, mMethodName, mParamsTypes);
      } catch (NoSuchMethodException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
          return getDeclaredMethodFromClassTree(parent);
        }
      }

      throw new Exception("not found mMethod: " + mMethodName + " from class: " + clazz);
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
