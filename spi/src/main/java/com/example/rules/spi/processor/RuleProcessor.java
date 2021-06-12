package com.example.rules.spi.processor;

import com.example.rules.api.RuleRequest;

/**
 * Base interface for a rule processor
 *
 * @param <R> the RuleRequest class associated with this processor
 */
public interface RuleProcessor<R extends RuleRequest> /*extends Worker, Initializable*/ {

    /**
     * Retrieves the RuleRequest class associated with this processor
     *
     * @return the RulesRequest class
     */
    Class<R> getRequestClass();
}
