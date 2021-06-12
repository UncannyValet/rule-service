package com.example.rules.spi.investigator;

import com.daxtechnologies.services.trace.Trace;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.spi.processor.RulesProcessor;
import com.spirent.cem.rules.spi.session.RulesSession;

import java.util.Collection;

/**
 * Base interface for an Investigator processor
 *
 * @param <R> the RulesRequest class associated with this processor
 */
public interface Investigator<R extends RulesRequest, F> extends RulesProcessor<R> {

    /**
     * Gets the fact class this Investigator provides
     *
     * @return a Class
     */
    Class<F> getFactClass();

    /**
     * Returns a flag indicating whether this investigator depends on the execution of any investigators in a set
     *
     * @param investigators the Set of investigators
     * @return {@code true} if a dependency exists, {@code false} otherwise
     */
    boolean dependsOn(Collection<? extends Investigator<R, ?>> investigators);

    /**
     * Sets the RulesSession into which this Investigator inserts facts
     *
     * @param session the session into which facts are inserted
     */
    void setSession(RulesSession session);

    /**
     * Sets the Trace used to monitor performance of this run
     * <br>Investigation may be done on threads other than the Arbiter's, but they need to be tracked together
     *
     * @param trace a Trace
     */
    void setTrace(Trace trace);

    /**
     * Configures the Investigator with its annotated options
     *
     * @param factory the InvestigatorFactory providing this Arbiter
     * @param options the InvestigatorOptions for this Investigator
     */
    void setOptions(InvestigatorFactory<R, F, ? extends Investigator<R, F>> factory, InvestigatorOptions options);

    /**
     * Gather facts for insertion into the RulesSession
     */
    void gatherFacts();

    /**
     * Returns a flag indicating whether there are additional facts available from this Investigator
     * <br>This flag is intended for use in batch operations where groups of facts are gathered and processed sequentially
     *
     * @return {@code true} if additional facts are available, {@code false} otherwise
     */
    default boolean dataAvailable() {
        return false;
    }
}
