package com.example.rules.fact;

import com.example.rules.api.RuleRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRequest implements RuleRequest {

    private final int threshold;

    public TestRequest(int threshold) {
        this.threshold = threshold;
    }
}
