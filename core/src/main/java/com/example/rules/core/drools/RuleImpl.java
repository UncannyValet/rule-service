package com.example.rules.core.drools;

import com.example.rules.spi.compiler.Rule;

import java.util.HashMap;
import java.util.Map;

public class RuleImpl implements Rule {

    private final String id;
    private String text;

    private RuleImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public static class Builder implements Rule.Builder {

        private final String id;
        private final Map<String, String> attributes = new HashMap<>();
        private String lhs;
        private String rhs;

        public Builder(String id) {
            this.id = id;
        }

        public Builder attribute(String name, String value) {
            attributes.put(name, value);
            return this;
        }

        public Builder when(String condition) {
            lhs = condition;
            return this;
        }

        public Builder then(String statement) {
            rhs = statement;
            return this;
        }

        public Rule build() {
            RuleImpl rule = new RuleImpl(id);

            StringBuilder sb = new StringBuilder();

            sb.append("rule \"").append(id).append("\"\n");
            attributes.forEach((name, value) -> sb.append("@").append(name).append("(").append(value).append(")\n"));
            sb.append("when\n")
                    .append(lhs)
                    .append("\nthen\n")
                    .append(rhs)
                    .append("\nend\n");

            rule.text = sb.toString();

            return rule;
        }
    }
}
