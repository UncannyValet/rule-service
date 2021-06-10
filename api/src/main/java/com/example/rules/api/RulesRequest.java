package com.example.rules.api;

import java.io.Serializable;

/**
 * A marker interface for a rules run request.  Implementations should define all of
 * the information needed for the rules engine to gather facts and execute rules against them.
 * <p>Implementations should also override hashCode() and equals() methods to allow the service
 * to find previous rules runs that were executed against a particular request.</p>
 */
//@IndexSubclasses
public interface RulesRequest extends Serializable/*, Nameable*/ {

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
