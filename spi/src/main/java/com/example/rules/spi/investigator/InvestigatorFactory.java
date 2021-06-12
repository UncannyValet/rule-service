package com.example.rules.spi.investigator;

import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.spi.processor.RulesFactory;

/**
 * Base interface for an InvestigatorFactory
 *
 * @param <I> the Investigator class that this factory builds
 */
public interface InvestigatorFactory<R extends RulesRequest, F, I extends Investigator<R, F>> extends RulesFactory<R, I> {

    /**
     * Returns the Fact class for which this factory provides investigators
     *
     * @return a Fact class
     */
    Class<F> getFactClass();
}
