package com.example.rules.core.processor;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;

import java.io.Serializable;
import java.util.Collection;

public interface ArbiterFactory {

    Class<? extends Serializable> getResultClass(Class<? extends RuleRequest> requestClass);

    <R extends RuleRequest, A extends Arbiter<R, ? extends Serializable>> A getArbiter(RuleContext context);

    Collection<Class<? extends RuleRequest>> getKnownRequests();
}
