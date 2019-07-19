package io.l0neman.utils.stay.reflect.mirror.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by l0neman on 2019/07/06.
 *
 * Specify the name of the target mirror class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetMirrorClassName {
  String value();
}
