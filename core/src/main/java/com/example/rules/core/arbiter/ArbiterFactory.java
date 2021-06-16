package com.example.rules.core.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;

public interface ArbiterFactory {

    Class<? extends RuleResult> getResultClass(RuleRequest request);

    Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass);

    <R extends RuleRequest, A extends Arbiter<R, ? extends RuleResult>> A getArbiter(RuleContext context);
}
