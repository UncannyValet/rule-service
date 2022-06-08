package com.example.rules.spi.utils;

import com.example.rules.api.RuleException;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.*;
import java.util.Map;

public class ClassUtils {

    private ClassUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypeArgument(Class<?> subType, Class<?> superType, int index) {
        Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(subType, superType);
        Type typeArgument = typeArguments.get(superType.getTypeParameters()[index]);
        if (typeArgument instanceof Class) {
            return (Class<T>)typeArgument;
        } else {
            return null;
        }
    }

    public static <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuleException("Failed to instantiate object " + clazz.getName(), e);
        }
    }

    public static boolean canInstantiate(Class<?> clazz) {
        boolean isAbstract = (clazz.getModifiers() & Modifier.ABSTRACT) != 0;
        boolean isInterface = (clazz.getModifiers() & Modifier.INTERFACE) != 0;
        return (!isAbstract && !isInterface);
    }
}
