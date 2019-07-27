package io.l0neman.utils.stay.reflect.mirror.util;


import androidx.collection.ArrayMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by l0neman on 2019/07/19.
 */
public class MirrorClassInfo {
  private Class<?> mMirrorClassName;
  private Map<String, Method> mMethodsInfo = new ArrayMap<>();
  private Map<String, Field> mFieldsInfo = new ArrayMap<>();
  private Map<String, Constructor> mConstructorInfo = new ArrayMap<>();

  public void setTargetMirrorClass(Class<?> mMirrorClassName) {
    this.mMirrorClassName = mMirrorClassName;
  }

  public Class<?> getTargetMirrorClass() {
    return mMirrorClassName;
  }

  public void putMethod(String methodSignature, Method method) {
    mMethodsInfo.put(methodSignature, method);
  }

  public Method getMethod(String methodSignature) {
    return mMethodsInfo.get(methodSignature);
  }

  public void putField(String fieldName, Field field) {
    mFieldsInfo.put(fieldName, field);
  }

  public Field getField(String fieldName) {
    return mFieldsInfo.get(fieldName);
  }

  public void putConstructor(String methodSignature, Constructor constructor) {
    mConstructorInfo.put(methodSignature, constructor);
  }

  public Constructor getConstructor(String methodSignature) {
    return mConstructorInfo.get(methodSignature);
  }
}
