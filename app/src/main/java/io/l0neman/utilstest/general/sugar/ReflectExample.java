package io.l0neman.utilstest.general.sugar;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.l0neman.utils.general.reflect.Reflect;

public class ReflectExample {

  private static final class ReflectTarget {
    private static String strField0 = "class string field.";
    private String strField1 = "instance string field.";

    private ReflectTarget() {}

    public ReflectTarget(String strField) {
      this.strField1 = strField;
    }

    private static String strMethod0(int a, int b) {
      final String str = "" + a + b;
      System.out.println(str);
      return str;
    }

    private String strMethod1(int a) {
      final String s = strField0 + a;
      System.out.println(s);
      return s;
    }
  }

  public void test() throws Exception {
    String targetClassName = "io.l0neman.utils.general.reflect.Reflect$ReflectTarget";
    ReflectTarget reflectTarget;
// 1. creator - 对象创建。

// start with a class.
    reflectTarget = Reflect.with(ReflectTarget.class).constructor()
        .parameterTypes(String.class)
        .create("hello");

// start with the class name.
    reflectTarget = Reflect.with(targetClassName).constructor()
        .parameterTypes(String.class)
        .create("hello");

// wrap java.lang.reflect.Constructor.
    Constructor<?> constructor = ReflectTarget.class.getConstructor(String.class);
    reflectTarget = Reflect.with(constructor).create("hello");


// 2. invoker - 方法调用。
// call object method.
    String result = Reflect.with(reflectTarget)
        .method("strMethod0")
        .parameterTypes(int.class)
        .invoke(1);

// call class method.
    result = Reflect.with(ReflectTarget.class)
        .method("strMethod0")
        .targetObject(reflectTarget)
        .parameterTypes(int.class, int.class)
        .invoke(1, 2);

// wrap java.lang.reflect.Method.
    final Method strMethod1 = ReflectTarget.class.getDeclaredMethod("strMethod1", int.class);

    result = Reflect.with(strMethod1)
        .parameterTypes(int.class, int.class)
        .invoke(1, 2);

// 3. injector - 成员读写。
// set object field.
    Reflect.with(reflectTarget)
        .field("strField1")
        .set("hello1");

// set class field.
    Reflect.with(Reflect.class)
        .field("strField0")
        .set("hello0");

// get object field.
    String strField = Reflect.with(reflectTarget)
        .field("strField1")
        .get();

// get class field.
    strField = Reflect.with(ReflectTarget.class)
        .field("strField0")
        .get();

// wrap java.lang.reflect.Field.
    Field strField1 = ReflectTarget.class.getDeclaredField("strField1");

    Reflect.with(strField1).targetObject(reflectTarget).set("hello");
    Reflect.with(strField1).targetObject(reflectTarget).get();

// 4. class for name.
    final Class<?> targetClass = Reflect.with(targetClassName).getClazz();
  }
}
