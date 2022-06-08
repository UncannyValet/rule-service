package com.example.rules.fact;

import com.example.rules.api.RuleRequest;
import lombok.Data;

@Data
public class TestRequestB implements RuleRequest {

    private int value;

    public void add(int value) {
        this.value += value;
    }
}
