package io.l0neman.utils.stay.uiautomation;

/**
 * Created by l0neman on 2019/11/28.
 */
public abstract class XCondition<T, R> {
  public abstract R apply(T arg);
}
