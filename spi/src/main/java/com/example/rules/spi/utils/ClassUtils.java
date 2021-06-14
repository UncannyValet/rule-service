package com.example.rules.spi.utils;

import com.example.rules.api.RuleException;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import static com.example.rules.api.ErrorNumbers.PROCESSOR_INSTANTIATION_FAILURE;

public class ClassUtils {

    private ClassUtils() {
    }

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
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuleException(e, PROCESSOR_INSTANTIATION_FAILURE);
        }
    }

    public static boolean canInstantiate(Class<?> clazz) {
        boolean isAbstract = (clazz.getModifiers() & Modifier.ABSTRACT) != 0;
        boolean isInterface = (clazz.getModifiers() & Modifier.INTERFACE) != 0;
        return (!isAbstract && !isInterface);
    }
}
