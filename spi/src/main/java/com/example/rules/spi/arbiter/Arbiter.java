package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import org.atteo.classindex.IndexSubclasses;

/**
 * Base interface for an Arbiter processor
 *
 * @param <R> the RuleRequest class associated with this processor
 * @param <O> the RuleResult class associated with this processor
 */
@IndexSubclasses
public interface Arbiter<R extends RuleRequest, O extends RuleResult> {

    /**
     * Executes a rules run against the configured set of rules
     *
     * @return a RuleResult
     */
    O processRules();
}
