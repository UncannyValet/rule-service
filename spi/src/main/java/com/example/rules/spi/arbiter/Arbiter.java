package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import org.atteo.classindex.IndexSubclasses;

import java.io.Serializable;

/**
 * Base interface for an Arbiter processor
 *
 * @param <R> the RuleRequest class associated with this processor
 * @param <O> the result class associated with this processor
 */
@IndexSubclasses
public interface Arbiter<R extends RuleRequest, O extends Serializable> {

    /**
     * Executes a rules run against the configured set of rules
     *
     * @return a result representing the output of the rules
     */
    O processRules();
}
