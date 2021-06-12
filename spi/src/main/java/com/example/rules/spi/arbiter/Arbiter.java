package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.processor.RuleProcessor;

import java.util.Collection;

/**
 * Base interface for an Arbiter processor
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <O> the RulesResult class associated with this processor
 */
public interface Arbiter<R extends RuleRequest, O extends RuleResult> extends RuleProcessor<R> {

    /**
     * Executes a rules run against the configured set of rules
     *
     * @return a Collection of child requests to process
     */
    Collection<RuleRequest> processRules();

    /**
     * Retrieves the RulesResult class associated with this arbiter
     *
     * @return the RulesResult class
     */
    Class<O> getResultClass();

    /**
     * Configures the Arbiter with its annotated options
     *
     * @param factory the ArbiterFactory providing this Arbiter
     * @param options the ArbiterOptions for this Arbiter
     */
    void setOptions(ArbiterFactory<R, O, ? extends Arbiter<R, O>> factory, ArbiterOptions options);
}
