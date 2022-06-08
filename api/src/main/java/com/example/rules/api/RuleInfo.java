package com.example.rules.api;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RuleInfo {

    public enum Type {
        RULE,
        QUERY,
        FUNCTION,
        FACT,
        GLOBAL
    }

    private String pkg;
    private String id;
    private Type type;
    private String source;
    private String session;
    private Map<String, Object> attributes;

    @Override
    public String toString() {
        return "RuleInfo{" +
                "pkg='" + pkg + '\'' +
                ", id='" + id + '\'' +
                ", type=" + type +
                '}';
    }
}
