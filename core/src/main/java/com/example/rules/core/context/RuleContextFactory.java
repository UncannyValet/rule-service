package com.example.rules.core.context;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;

public interface RuleContextFactory {

    RuleContext newContext(RuleRequest request);
}
