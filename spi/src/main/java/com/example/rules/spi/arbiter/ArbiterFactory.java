package com.example.rules.spi.arbiter;

import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;
import com.spirent.cem.rules.spi.processor.RulesFactory;

/**
 * Base interface for an ArbiterFactory
 *
 * @param <A> the Arbiter class that this factory builds
 */
public interface ArbiterFactory<R extends RulesRequest, O extends RulesResult, A extends Arbiter<R, O>> extends RulesFactory<R, A> {

    /**
     * Returns the RulesResult class for which this factory provides arbiters
     *
     * @return a RulesResult class
     */
    Class<O> getResultClass();
}
