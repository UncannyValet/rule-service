package com.example.rules.fact;

import com.example.rules.api.RuleRequest;
import lombok.Data;

@Data
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

}
