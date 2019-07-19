package io.l0neman.utils.stay.reflect.mirror.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the parameter type of the constructor or method,
 * supporting three overloads.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MethodParameterClassesOverload2 {
  String[] overload0();

  String[] overload1();

  String[] overload2();
}
