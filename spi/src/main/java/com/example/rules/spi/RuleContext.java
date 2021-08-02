package com.example.rules.spi;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.session.RuleSession;

import java.io.Serializable;

/**
 * A context containing information about a rule run
 */
public interface RuleContext {

    /**
     * Returns the ID of the run to which this Context applies
     *
     * @return the ID of the run to
     */
    long getId();

    /**
     * Retrieves the RulesRequest for this session
     *
     * @return the current RulesRequest
     */
    <T extends RuleRequest> T getRequest();

    /**
     * Retrieves the current result, can be null if no result has been set yet
     *
     * @return the current result
     */
    <T extends Serializable> T getResult();

    /**
     * Updates the result, making it available to service calls
     *
     * @param result the result to provide
     */
    void setResult(Serializable result);

    /**
     * Sets an arbitrary attribute in the Context, available to all later processors
     *
     * @param name  the name of the attribute
     * @param value the value to set
     */
    void setAttribute(String name, Object value);

    /**
     * Retrieves an arbitrary attribute previously set
     *
     * @param name the name of the attribute
     * @return the attribute value, or null if not set
     */
    <T> T getAttribute(String name);

    RuleStats getStats();

    /**
     * Creates a new RuleSession, based on the sessionIds requested and any other IDs registered with the request
     *
     * @param sessionIds the IDs of rule sessions to provide
     * @return a RuleSession encapsulating sessions for the provided IDs
     */
    RuleSession newSession(String... sessionIds);

    /**
     * Spawns Investigators in parallel to gather facts, accounting for dependencies if any exist
     */
    void investigate(RuleSession session);

    boolean isStopped();
}
