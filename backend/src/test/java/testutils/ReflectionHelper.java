package testutils;

import java.lang.reflect.Field;

public class ReflectionHelper {
    static void setStatic(Field field, Object newValue) {
        field.setAccessible(true);

        try {
            field.set(null, newValue);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Field: " + field + ", cannot be set to: " + newValue, e);
        }
    }

    public static void setStatic(String fieldName, Class<?> type, Object newValue) {
        Field field;
        try {
            field = type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field: " + fieldName + " is not present in " + type.getName(), e);
        }

        setStatic(field, newValue);
    }
}
