package com.braintreepayments.api.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {

    public static Object getField(Object src, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getField(src.getClass(), src, fieldName);
    }

    public static Object getField(Class clazz, Object src, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(src);
    }

    public static void setField(Object src, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        setField(src.getClass(), src, fieldName, value);
    }

    public static void setField(Class clazz, Object src, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(src, value);
    }
}
