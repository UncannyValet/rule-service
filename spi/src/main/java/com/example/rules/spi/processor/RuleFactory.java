package com.example.rules.spi.processor;

//import com.daxtechnologies.util.Initializable;
//import com.daxtechnologies.util.Nameable;
import com.example.rules.api.RuleRequest;

/**
 * Base interface for a rule processor factory
 *
 * @param <P> the RuleProcessor class that this factory builds
 */
public interface RuleFactory<R extends RuleRequest, P extends RuleProcessor<R>> /*extends Initializable, Nameable*/ {

    /**
     * Returns the processor class
     *
     * @return a non-null instance
     */
    Class<P> getProcessorClass();

    /**
     * Creates a new instance of a processor
     *
     * @return a processor instance
     */
    P newProcessor();

    /**
     * Returns the RuleRequest class for which this factory provides processors
     *
     * @return a RuleRequest class
     */
    Class<R> getRequestClass();
}
