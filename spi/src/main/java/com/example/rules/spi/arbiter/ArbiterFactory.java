package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;

/**
 * Base interface for an ArbiterFactory
 *
 * @param <A> the Arbiter class that this factory builds
 */
public interface ArbiterFactory<R extends RuleRequest, O extends RuleResult, A extends Arbiter<R, O>> /*extends RuleFactory<R, A>*/ {

    Class<R> getRequestClass();

    /**
     * Returns the RulesResult class for which this factory provides arbiters
     *
     * @return a RulesResult class
     */
    Class<O> getResultClass();

    A newArbiter();
}
