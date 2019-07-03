package io.l0neman.utilstest.general.sugar;

import io.l0neman.utils.general.sugar.Reflect;

public class ReflectExample {

  private static final class Target {
    private String str = "123";
    public static int sInt = 7;

    private int getNumber(Target target) {
      return target.hashCode();
    }

    public static String getStr() {
      return "testStr";
    }
  }

  public void test() {
    Target target = new Target();

//    /* 1-1. 操作对象的成员变量 */
    try {
      // 注入。
      Reflect.with(target).injector()
          .field("str")
          .set("rts");

    } catch (Exception e) {
      /* 如果需要判断异常类型 */
      if (e instanceof NoSuchFieldException) {
      } else
        if (e instanceof IllegalAccessException) {
        } else { throw new AssertionError("UNKNOWN ERROR."); }
    }

    try {
      // 读取。
      String str = Reflect.with(target).injector()
          .field("str")
          .get();
    } catch (Exception ignore) {}

    /* 1-2. 操作类的静态成员变量 */
    try {
      Reflect.with(Target.class).injector()
          .field("sInt")
          .set(2);
    } catch (Exception ignore) {}

    try {
      int sInt = Reflect.with(Target.class).injector()
          .field("sInt")
          .get();
    } catch (Exception ignore) {}

    /* 2-1. 操作对象的方法 */
    try {
      int hashCode = Reflect.with(target).invoker()
          .method("getNumber")
          .paramsType(Target.class)
          .invoke(target);
    } catch (Exception ignore) {}

    /* 2-2. 操作类的静态方法 */
    try {
      String str = Reflect.with(Target.class).invoker()
          .method("getStr")
          .invoke();
    } catch (Exception ignore) {}
  }
}
