package io.l0neman.utils.stay.reflect.mirror.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the parameter type of the constructor or method,
 * supporting two overloads.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MethodParameterTypesOverload {
  Class<?>[] overload0();

  Class<?>[] overload1();
}
