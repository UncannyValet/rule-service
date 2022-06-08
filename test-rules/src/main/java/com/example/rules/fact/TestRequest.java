package com.example.rules.fact;

import com.example.rules.api.RuleRequest;
import lombok.Data;

@Data
public class TestRequest implements RuleRequest {

    private final int threshold;

}
