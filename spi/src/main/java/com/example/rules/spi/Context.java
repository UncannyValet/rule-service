package com.example.rules.spi;

import com.example.rules.api.FactStatistic;
import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.session.RuleSession;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * A context containing information about a rules run
 */
public interface Context {

    /**
     * Returns the ID of the run to which this Context applies
     *
     * @return the ID of the run to
     */
    String getId();

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
     * Returns a Map of Rule IDs and their assertion count
     *
     * @return a Map of Rule IDs and their assertion count
     */
    Map<String, Integer> getRuleStatistics();

    /**
     * Returns a Map of fact classes and their count and duration
     *
     * @return a Map of fact classes and their count and duration
     */
    Map<String, FactStatistic> getFactStatistics();

    /**
     * Resolves a dimension by ID
     *
     * @param dimensionClass the dimension Class
     * @param id             the id of the dimension entry
     * @param <R>            the dimension Class
     * @return a dimension entry, or null if no match exists
     */
    <R> R resolveDimension(Class<R> dimensionClass, Object id);

    <R extends RuleRequest> Future<Investigator<R, ?>> scheduleInvestigation(Investigator<R, ?> investigator, RuleSession session);

    /**
     * Gets a count of the asserted rules
     *
     * @return a count of the asserted rules
     */
    int getRuleCount();

    /**
     * Gets a count of the rules asserted by a given Arbiter class
     *
     * @param clazz the Arbiter class to count asserted rules
     * @return a count of the asserted rules
     */
    <A extends Arbiter<?, ?>> int getRuleCount(Class<A> clazz);

    /**
     * Gets a total duration of the rules processing
     *
     * @return a duration in ms
     */
    long getRuleDuration();

    /**
     * Gets the total duration of rules processing by a given Arbiter class
     *
     * @param arbiterClass the Arbiter class to retrieve the duration for
     * @return a duration in ms
     */
    <A extends Arbiter<?, ?>> long getRuleDuration(Class<A> arbiterClass);

    /**
     * Starts timing rules processing for a given Arbiter class
     *
     * @param arbiterClass the Arbiter class
     */
    <A extends Arbiter<?, ?>> void startRules(Class<A> arbiterClass);

    /**
     * Finish timing rules processing for a given Arbiter class
     *
     * @param arbiterClass the Arbiter class
     * @param session      the RulesSession that ran the rules
     */
    <A extends Arbiter<?, ?>> void finishRules(Class<A> arbiterClass, RuleSession session);

    /**
     * Gets a count of inserted facts
     *
     * @return a count of inserted facts
     */
    int getFactCount();

    /**
     * Gets a count of facts inserted of a given Fact class
     *
     * @param factClass the Fact class counted
     * @return a count of the inserted facts
     */
    int getFactCount(Class<?> factClass);

    /**
     * Gets a total duration of fact gathering
     *
     * @return a duration in ms
     */
    long getFactDuration();

    /**
     * Gets the total duration of fact gathering for a given Fact class
     *
     * @param factClass the Fact class to retrieve the duration for
     * @return a duration in ms
     */
    long getFactDuration(Class<?> factClass);

    /**
     * Starts timing gathering for a given Fact class
     *
     * @param factClass the Fact class
     */
    void startFacts(Class<?> factClass);

    /**
     * Finish timing gathering for a given Fact class
     *
     * @param factClass the Fact class
     * @param count     the total count of inserted facts
     */
    void finishFacts(Class<?> factClass, int count);

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
