package com.example.rules.fact;

import com.example.rules.api.RuleRequest;

public class CancelRequest implements RuleRequest {

    public enum Type {
        BEFORE_SESSION,
        BEFORE_FACTS,
        BEFORE_RULES,
        INVESTIGATION,
        RULES,
        AFTER_RULES,
        AFTER_SESSION,
        EXTERNAL
    }

    private final Type type;

    public CancelRequest(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
