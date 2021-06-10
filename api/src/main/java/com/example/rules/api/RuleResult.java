package com.example.rules.api;

import java.io.Serializable;

/**
 * A marker interface for a rule result.  Implementations should contain all of the structures
 * that a rule run will need to provide information on the rule outcomes.
 */
public interface RuleResult extends Serializable {

    default String getName() {
        return getClass().getSimpleName();
    }

    default String getDescription() {
        return getName() + "/" + getClass().getName();
    }
}
