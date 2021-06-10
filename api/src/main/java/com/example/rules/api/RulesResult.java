package com.example.rules.api;

import java.io.Serializable;

/**
 * A marker interface for a rules result.  Implementations should contain all of the structures
 * that a rules run will need to provide information on the rules outcomes.
 */
public interface RulesResult extends Serializable/*, Nameable*/ {

//    @Override
    default String getName() {
//        Named named = AnnotationUtilities.getAnnotation(getClass(), Named.class);
//        if (named != null) {
//            return named.value();
//        }
        return getClass().getSimpleName();
    }

//    @Override
    default String getDescription() {
        return getName() + "/" + getClass().getName();
    }
}
