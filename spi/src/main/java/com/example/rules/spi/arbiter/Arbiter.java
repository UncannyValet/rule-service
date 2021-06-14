package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.Context;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.session.RuleSession;
//import com.example.rules.spi.processor.RuleProcessor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Base interface for an Arbiter processor
 *
 * @param <R> the RuleRequest class associated with this processor
 * @param <O> the RuleResult class associated with this processor
 */
public interface Arbiter<R extends RuleRequest, O extends RuleResult> extends Callable<Void> /*extends RuleProcessor<R>*/ {

    void initialize(R request, Context context, RuleSession session, List<Investigator<R, ?>> investigators);

    /**
     * Executes a rules run against the configured set of rules
     *
     * @return a Collection of child requests to process
     */
    Collection<RuleRequest> runRules();

//    /**
//     * Retrieves the RulesResult class associated with this arbiter
//     *
//     * @return the RulesResult class
//     */
//    Class<O> getResultClass();

//    /**
//     * Configures the Arbiter with its annotated options
//     *
//     * @param factory the ArbiterFactory providing this Arbiter
//     * @param options the ArbiterOptions for this Arbiter
//     */
//    void setOptions(ArbiterFactory<R, O, ? extends Arbiter<R, O>> factory, ArbiterOptions options);
}
