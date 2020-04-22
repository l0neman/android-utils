package io.l0neman.utils.general.reflect;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by l0neman on 2019/06/23.
 *
 * @see <a href="l0neman - Reflect">https://github.com/l0neman/MyPublicTools/blob/master/utilslib/src/main/java/io/l0neman/utils/general/reflect/desc_reflect_utils.md</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Reflect {
  private static ThreadLocal<Reflect> sThreadState =
      new ThreadLocal<Reflect>() {
        @Override protected Reflect initialValue() {
          return new Reflect();
        }
      };

  private Reflector reflector = new Reflector();

  public static class Reflector {
    Class<?> mClass;
    Object mObject;

    protected final Class<?> getClazz() {
      if (mClass != null) { return mClass; }

      return mObject != null ? mObject.getClass() : null;
    }
  }

  public static class ReflectException extends Exception {

    public ReflectException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public Class<?> getClazz() {
    return reflector.getClazz();
  }

  private static void checkNull(Object obj, String tag) {
    if (obj == null) {
      throw new NullPointerException(tag + " is null.");
    }
  }

  public static Reflect with(String className) {
    checkNull(className, "class name");
    try {
      return with(Class.forName(className));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Reflect with(Object object) {
    checkNull(object, "object");
    final Reflect reflect = sThreadState.get();
    reflect.clean();
    reflect.reflector.mClass = object.getClass();
    reflect.reflector.mObject = object;
    return reflect;
  }

  public static Reflect with(Class<?> clazz) {
    checkNull(clazz, "class");
    final Reflect reflect = sThreadState.get();
    reflect.clean();
    reflect.reflector.mClass = clazz;
    return reflect;
  }

  public static Creator with(Constructor<?> constructor) {
    checkNull(constructor, "constructor");
    return new Creator(constructor);
  }

  public static Injector with(Field field) {
    checkNull(field, "field");
    return new Injector(field);
  }

  public static Invoker with(Method method) {
    checkNull(method, "method");
    return new Invoker(method);
  }

  public final Injector field(String fieldName) {
    checkNull(fieldName, "field name");
    final Injector injector = new Injector(fieldName);
    injector.mClass = reflector.mClass;
    injector.mObject = reflector.mObject;
    return injector;
  }

  public final Invoker method(String methodName) {
    checkNull(methodName, "method name");
    final Invoker invoker = new Invoker(methodName);
    invoker.mClass = reflector.mClass;
    invoker.mObject = reflector.mObject;
    return invoker;
  }

  public final Creator constructor() {
    final Creator creator = new Creator();
    creator.mClass = reflector.mClass;
    creator.mObject = reflector.mObject;
    return creator;
  }

  // clean temp
  private void clean() {
    reflector.mClass = null;
    reflector.mObject = null;
  }

  public static final class Creator extends Reflector {
    private Constructor<?> constructor;
    private Class<?>[] constructorParameterTypes;

    private Creator() {}

    private Creator(Constructor<?> constructor) {
      this.constructor = constructor;
    }

    public Creator parameterTypes(Class<?>... constructorParameterTypes) {
      this.constructorParameterTypes = constructorParameterTypes;
      return this;
    }

    public Constructor<?> getConstructor() throws ReflectException {
      try {
        // noinspection ConstantConditions
        final Constructor<?> constructor = getClazz().getConstructor(constructorParameterTypes);
        constructor.setAccessible(true);
        return constructor;
      } catch (Exception e) {
        throw new ReflectException("Creator getConstructor", e);
      }
    }

    public <T> T create(Object... args) throws ReflectException {
      try {
        if (constructor != null) {
          // noinspection unchecked
          return (T) constructor.newInstance(args);
        }

        if (constructorParameterTypes == null) {
          constructorParameterTypes = getTypesFromObjects();
        }

        // noinspection ConstantConditions
        final Constructor<?> constructor = getClazz().getConstructor(constructorParameterTypes);

        // noinspection unchecked
        return (T) constructor.newInstance(args);
      } catch (Exception e) {
        throw new ReflectException("#create", e);
      }
    }
  }

  public static final class Injector extends Reflector {
    private Field mField;
    private String mFieldName;

    private Injector(Field mField) {
      this.mField = mField;
    }

    private Injector(String mFieldName) {
      this.mFieldName = mFieldName;
    }

    public Injector targetObject(Object object) {
      this.mObject = object;
      return this;
    }

    public void setQuietly(Object value) {
      try {
        set(value);
      } catch (ReflectException e) {
        throw new RuntimeException(e);
      }
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
        throw new ReflectException("#set", e);
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
        throw new ReflectException("#getField", e);
      }
    }

    public <T> T getQuietly() {
      try {
        return get();
      } catch (ReflectException e) {
        throw new RuntimeException(e);
      }
    }

    public <T> T get() throws ReflectException {
      try {
        if (mField != null) {
          // noinspection unchecked: throw cast exception.
          return (T) mField.get(mObject);
        }

        Field field = getDeclaredFieldFromClassTree(mClass);
        field.setAccessible(true);

        // noinspection unchecked: throw cast exception.
        return (T) field.get(mObject);
      } catch (Exception e) {
        throw new ReflectException("#get", e);
      }
    }

    private Field getDeclaredFieldFromClassTree(Class<?> clazz) throws Exception {
      try {
        return clazz.getDeclaredField(mFieldName);
      } catch (NoSuchFieldException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
          return getDeclaredFieldFromClassTree(parent);
        }
      }

      throw new Exception("not found field: " + mFieldName + " from class: " + clazz);
    }
  }

  public static final class Invoker extends Reflector {
    private Method mMethod;
    private String mMethodName;
    private Class<?>[] mParameterTypes;

    private Invoker(Method mMethod) {
      this.mMethod = mMethod;
    }

    private Invoker(String mMethodName) {
      this.mMethodName = mMethodName;
    }

    public Invoker targetObject(Object object) {
      this.mObject = object;
      return this;
    }

    /**
     * Sets the type of the mMethod's arguments.
     *
     * @param parameterTypes methods parameter types.
     * @return self
     */
    public Invoker parameterTypes(Class<?>... parameterTypes) {
      this.mParameterTypes = parameterTypes;
      return this;
    }

    public Method getMethod() throws ReflectException {
      try {
        if (mMethod != null) {
          return mMethod;
        }

        Method targetMethod = getDeclaredMethodFromClassTree(mClass, null);
        targetMethod.setAccessible(true);

        return targetMethod;
      } catch (Exception e) {
        throw new ReflectException("#getMethod", e);
      }
    }

    public <T> T invokeQuietly(Object... params) {
      try {
        return invoke(params);
      } catch (ReflectException e) {
        throw new RuntimeException(e);
      }
    }

    public <T> T invoke(Object... args) throws ReflectException {
      try {
        if (mMethod != null) {
          // noinspection unchecked: throw cast exception.
          return (T) mMethod.invoke(mObject, args);
        }

        Method targetMethod = getDeclaredMethodFromClassTree(mClass, args);
        targetMethod.setAccessible(true);

        // noinspection unchecked: throw cast exception.
        return (T) targetMethod.invoke(mObject, args);
      } catch (Exception e) {
        throw new ReflectException("#invoke", e);
      }
    }

    private Method getDeclaredMethodFromClassTree(Class<?> clazz, Object args) throws Exception {
      if (mParameterTypes == null) {
        if (args != null) {
          mParameterTypes = getTypesFromObjects(args);
        }

        throw new IllegalArgumentException("parameterTypes and args are both null.");
      }

      try {
        return clazz.getDeclaredMethod(mMethodName, mParameterTypes);
      } catch (NoSuchMethodException e) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
          return getDeclaredMethodFromClassTree(parent, args);
        }
      }

      throw new Exception("not found method: " + mMethodName + " from class: " + clazz);
    }
  }

  private static boolean sBypassedP;

  private static Class<?> getVMRuntimeClass() {
    Method forNameMethod;
    try {
      forNameMethod = Class.class.getDeclaredMethod("forName", String.class);
      return (Class<?>) forNameMethod.invoke(null, "dalvik.system.VMRuntime");
    } catch (Exception e) {
      return null;
    }
  }

  private static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>[] parameterTypes) {
    try {
      Method getMethodMethod = Class.class.getDeclaredMethod("getDeclaredMethod",
          String.class, Class[].class);
      return (Method) getMethodMethod.invoke(clazz, name, parameterTypes);
    } catch (Exception e) {
      return null;
    }
  }

  private static String getEmuiSystemProperties() {
    try {
      return Reflect.with("android.os.SystemProperties").method("get")
          .parameterTypes(String.class).invoke("ro.build.version.emui");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private static boolean isEMUI() {
    if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) { return true; }

    String property = getEmuiSystemProperties();
    return property != null && property.contains("EmotionUI");
  }

  public static void bypassHiddenAPIEnforcementPolicyIfNeeded() {
    if (sBypassedP) { return; }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      try {
        Class<?> clazz = getVMRuntimeClass();
        Method getRuntime = getDeclaredMethod(clazz, "getRuntime", new Class[0]);
        Method setHiddenApiExemptions = getDeclaredMethod(clazz, "setHiddenApiExemptions",
            new Class[]{String[].class});
        if (getRuntime == null || setHiddenApiExemptions == null) {
          throw new NullPointerException("keys are null.");
        }

        Object runtime = getRuntime.invoke(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isEMUI()) {
          setHiddenApiExemptions.invoke(runtime, new Object[]{
              new String[]{
                  "Landroid/",
                  "Lcom/android/",
                  "Ljava/lang/",
                  "Ldalvik/system/",
                  "Llibcore/io/",
                  "Lhuawei/"
              }
          });
        } else {
          setHiddenApiExemptions.invoke(runtime, new Object[]{
              new String[]{
                  "Landroid/",
                  "Lcom/android/",
                  "Ljava/lang/",
                  "Ldalvik/system/",
                  "Llibcore/io/"
              }
          });
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    sBypassedP = true;
  }

  private static Class<?>[] getTypesFromObjects(Object... values) throws Exception {
    if (values == null) { return new Class[0]; }

    Class<?>[] result = new Class[values.length];

    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      if (value == null) {
        throw new Exception("values[" + i + "] is null.");
      }

      result[i] = value.getClass();
    }

    return result;
  }

  public static Object invokeQuietly(Method method, Object who, Object... args) {
    try {
      return method.invoke(who, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
