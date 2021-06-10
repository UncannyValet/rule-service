package com.example.rules.api;

//import com.daxtechnologies.record.AbstractRecord;

import java.util.Map;

public class RuleInfo /*extends AbstractRecord<RuleInfo>*/ {

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

    @SuppressWarnings("squid:S1948")
    private Map<String, Object> attributes;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "RuleInfo{" +
                "pkg='" + pkg + '\'' +
                ", id='" + id + '\'' +
                ", type=" + type +
                '}';
    }
}
