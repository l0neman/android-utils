# ReflectUtils

[源码 - Reflect.java](Reflect.java)

链式包装反射工具，用起来流畅自然。

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
  // 注入。
  Reflect.with(target).injector()
      .field("str")
      .set("rts");
} catch (Exception e) {
  /* 如果需要判断异常类型 */
  if (e instanceof NoSuchFieldException) {
  } else if (e instanceof IllegalAccessException) {
  } else { throw new AssertionError("UNKNOWN ERROR."); }
}
```

```java
try {
  // 读取。
  String str = Reflect.with(target).injector()
      .field("str")
      .get();
} catch (Exception ignore) {}
```

## 1-2. 操作类的静态成员变量

```java
try {
  // 注入。
  Reflect.with(Target.class).injector()
      .field("sInt")
      .set(2);
} catch (Exception ignore) {}
```

```java
try {
  // 读取。
  int sInt = Reflect.with(Target.class).injector()
      .field("sInt")
      .get();
} catch (Exception ignore) {}
```

## 2-1. 调用对象的方法

```java
try {
  int hashCode = Reflect.with(target).invoker()
      .method("getNumber")
      .paramsType(Target.class)
      .invoke(target);
} catch (Exception ignore) {}
```

## 2-2. 调用类的静态方法

```java
try {
  String str = Reflect.with(Target.class).invoker()
      .method("getStr")
      .invoke();
} catch (Exception ignore) {}
```

