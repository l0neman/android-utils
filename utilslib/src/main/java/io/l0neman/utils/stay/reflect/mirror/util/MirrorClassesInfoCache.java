package io.l0neman.utils.stay.reflect.mirror.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by l0neman on 2019/07/15.
 */
public class MirrorClassesInfoCache {

  private Map<Class<?>, ReflectClassInfo> mClassInfo = new HashMap<>();

  public static class ReflectClassInfo {
    private Map<String, Method> mMethodsInfo = new HashMap<>();
    private Map<String, Field> mFieldsInfo = new HashMap<>();

    public void saveMethod(String methodSignature, Method method) {
      mMethodsInfo.put(methodSignature, method);
    }

    public void saveField(String fieldName, Field field) {
      mFieldsInfo.put(fieldName, field);
    }

    public Method queryMethod(String methodSignature) {
      return mMethodsInfo.get(methodSignature);
    }

    public Field queryField(String fieldName) {
      return mFieldsInfo.get(fieldName);
    }
  }

  public ReflectClassInfo getReflectClassInfo(Class<?> clazz) {
    return mClassInfo.get(clazz);
  }

  public void saveReflectClassInfo(Class<?> clazz, ReflectClassInfo reflectClassInfo) {
    mClassInfo.put(clazz, reflectClassInfo);
  }
}
