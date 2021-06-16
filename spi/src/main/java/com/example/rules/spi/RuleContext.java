package com.example.rules.spi;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.session.RuleSession;

/**
 * A context containing information about a rule run
 */
public interface RuleContext extends RuleStats {

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
     * Retrieves the current RulesResult, can be null if no result has been set yet
     *
     * @return the current RulesResult
     */
    <T extends RuleResult> T getResult();

    /**
     * Updates the RulesResult, making it available to service calls
     *
     * @param result the RulesResult to provide
     */
    void setResult(RuleResult result);

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

    /**
     * Resolves a dimension by ID
     *
     * @param dimensionClass the dimension Class
     * @param id             the id of the dimension entry
     * @param <R>            the dimension Class
     * @return a dimension entry, or null if no match exists
     */
    <R> R resolveDimension(Class<R> dimensionClass, Object id);

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

    /**
     * Marks the Context as stopped, indicating that the underlying run has been cancelled
     */
    void stop();

    /**
     * Indicates whether this context has been marked as stopped
     *
     * @return {@code true} if stop() has been called, {@code false} otherwise
     */
    boolean isStopped();
}
