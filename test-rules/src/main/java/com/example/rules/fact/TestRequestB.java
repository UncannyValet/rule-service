package com.example.rules.fact;

import com.example.rules.api.RuleRequest;

public class TestRequestB implements RuleRequest {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void add(int value) {
        this.value += value;
    }
}
