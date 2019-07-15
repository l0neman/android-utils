# ReflectUtils

[源码 - Reflect.java](Reflect.java)

链式包装反射工具，用起来流畅自然。

- 示例目标对象：

```java
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
```

## creator - 对象创建

```java
String targetClassName = "io.l0neman.utils.general.reflect.Reflect$ReflectTarget";
```

```java
// start with a class.
reflectTarget = Reflect.with(ReflectTarget.class).creator()
    .parameterTypes(String.class)
    .create("hello");
```

```java
// start with the class name.
reflectTarget = Reflect.with(targetClassName).creator()
    .parameterTypes(String.class)
    .create("hello");
```

```java
// wrap java.lang.reflect.Constructor.
Constructor<?> constructor = ReflectTarget.class.getConstructor(String.class);
reflectTarget = Reflect.with(constructor).create("hello");
```

## invoker - 方法调用

```java
// call object method.
String result = Reflect.with(reflectTarget).invoker()
    .method("strMethod0")
    .paramsType(int.class)
    .invoke(1);
```

```java
// call class method.
result = Reflect.with(ReflectTarget.class).invoker()
    .method("strMethod0")
    .targetObject(reflectTarget)
    .paramsType(int.class, int.class)
    .invoke(1, 2);
```

```java
// wrap java.lang.reflect.Method.
final Method strMethod1 = ReflectTarget.class.getDeclaredMethod("strMethod1", int.class);

result = Reflect.with(strMethod1)
    .paramsType(int.class, int.class)
    .invoke(1, 2);
```

## injector - 成员读写

```java
// set object field.
Reflect.with(reflectTarget).injector()
    .field("strField1")
    .set("hello1");
```

```java
// set class field.
Reflect.with(Reflect.class).injector()
    .field("strField0")
    .set("hello0");
```

```java
// get object field.
String strField = Reflect.with(reflectTarget).injector()
    .field("strField1")
    .get();
```

```java
// get class field.
strField = Reflect.with(ReflectTarget.class).injector()
    .field("strField0")
    .get();
```    

```java
// wrap java.lang.reflect.Field.
Field strField1 = ReflectTarget.class.getDeclaredField("strField1");

Reflect.with(strField1).targetObject(reflectTarget).set("hello");
Reflect.with(strField1).targetObject(reflectTarget).get();
```

## class for name

```java
// 4. class for name.
final Class<?> targetClass = Reflect.with(targetClassName).getClazz();
```