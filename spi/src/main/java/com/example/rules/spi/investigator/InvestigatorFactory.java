package com.example.rules.spi.investigator;

import com.example.rules.api.RuleRequest;
//import com.example.rules.spi.processor.RulesFactory;

/**
 * Base interface for an InvestigatorFactory
 *
 * @param <I> the Investigator class that this factory builds
 */
public interface InvestigatorFactory<R extends RuleRequest, F, I extends Investigator<R, F>> /*extends RulesFactory<R, I>*/ {

    Class<R> getRequestClass();
    /**
     * Returns the Fact class for which this factory provides investigators
     *
     * @return a Fact class
     */
    Class<F> getFactClass();

    I newInvestigator();
}
