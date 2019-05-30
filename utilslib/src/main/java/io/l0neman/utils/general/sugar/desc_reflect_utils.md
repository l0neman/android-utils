# ReflectUtils

[源码 - ReflectUtils.java](ReflectUtils.java)

链式包装反射工具，使用简单直接。

- 示例中的目标对象：

```java
private static final class Target extends Base {
  private String str = "123";
  public static int sInt = 7;

  private int getNumber(Target target) {
    return target.hashCode();
  }

  public static String getStr() {
    return "testStr";
  }
}
```

## 1-1. 操作对象的成员变量

```java
try {
  // set value.
  ReflectUtils.inject(target)
      .field("str")
      .set("rts");
} catch (Exception e) {
  /* 如果需要判断异常类型 */
  if (e instanceof NoSuchFieldException) { }
  else if (e instanceof IllegalAccessException) { }
  else { throw new AssertionError("UNKNOWN ERROR."); }
}
```

```java
try {
  // get value.
  String str = (String) ReflectUtils.inject(target)
      .field("str")
      .get();
} catch (Exception ignore) {}
```

## 1-2. 操作类的静态成员变量

```java
try {
  // set value.
  ReflectUtils.inject(Target.class)
      .field("sInt")
      .set(2);
} catch (Exception ignore) {}
```

```java
try {
  // get value.
  int sInt = (int) ReflectUtils.inject(Target.class)
      .field("sInt")
      .get();
} catch (Exception ignore) {}
```

## 2-1. 调用对象的方法

```java
try {
  // call method.
  int hashCode = (int) ReflectUtils.invoke(target)
    .method("getNumber")
    .paramsType(Target.class)
    .invoke(target);
} catch (Exception ignore) {}
```

## 2-2. 调用类的静态方法

```java
try {
  // call static method.
  String str = (String) ReflectUtils.invoke(Target.class)
    .method("getStr")
    .invoke();
} catch (Exception ignore) {}
```

- 可回收内部的工具实例，Just an object。

```java
ReflectUtils.recycle();
```

