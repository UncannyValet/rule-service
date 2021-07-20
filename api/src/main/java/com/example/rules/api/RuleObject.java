package com.example.rules.api;

import java.io.Serializable;

public interface RuleObject extends Serializable {

    default String getName() {
        return getClass().getSimpleName();
    }

    default String getDescription() {
        return getName() + "/" + getClass().getName();
    }
}
