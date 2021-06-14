package com.example.rules.spi.investigator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface DependsUpon {

    /**
     * A set of investigators that this investigator depends on
     * <p>If these investigators are included in an investigation, this one will
     * wait until those have finished loading facts before continuing</p>
     */
    Class<? extends Investigator<?, ?>>[] value();
}
