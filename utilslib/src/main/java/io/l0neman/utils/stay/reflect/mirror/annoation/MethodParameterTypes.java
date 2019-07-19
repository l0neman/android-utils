package io.l0neman.utils.stay.reflect.mirror.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by l0neman on 2019/07/07.
 * <p>
 * Specify the parameter type of the constructor or method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MethodParameterTypes {
  Class<?>[] value();
}
