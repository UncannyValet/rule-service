package com.example.rules.spi.arbiter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface RuleSet {

    /**
     * A collection of rule session IDs to be executed by an Arbiter
     */
    String[] value();
}
