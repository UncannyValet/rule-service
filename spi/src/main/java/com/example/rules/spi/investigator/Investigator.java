package com.example.rules.spi.investigator;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.session.RuleSession;
import org.atteo.classindex.IndexSubclasses;

import java.util.Collection;

/**
 * Base interface for an Investigator processor
 *
 * @param <R> the RuleRequest class associated with this processor
 */
@IndexSubclasses
public interface Investigator<R extends RuleRequest, F> {

    /**
     * Gather facts for insertion into the RuleSession
     */
    void gatherFacts(RuleSession session);

    /**
     * Returns a flag indicating whether this investigator depends on the execution of any investigators in a set
     *
     * @param investigators the Set of investigators
     * @return {@code true} if a dependency exists, {@code false} otherwise
     */
    default boolean dependsOn(Collection<? extends Investigator<?, ?>> investigators) {
        return false;
    }
}
