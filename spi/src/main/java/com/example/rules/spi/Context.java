package com.example.rules.spi;

import com.daxtechnologies.database.orm.activerecord.ActiveRecord;
import com.spirent.cem.rules.api.RulesResult;
import com.spirent.cem.rules.spi.arbiter.Arbiter;
import com.spirent.cem.rules.spi.session.RulesSession;

/**
 * A context containing information about a rules run
 */
public interface Context extends RulesContext {

    /**
     * Updates the RulesResult, making it available to service calls
     *
     * @param result the RulesResult to provide
     */
    void setResult(RulesResult result);

    /**
     * Sets an arbitrary attribute in the Context, available to all later processors
     *
     * @param name  the name of the attribute
     * @param value the value to set
     */
    void setAttribute(String name, Object value);

    /**
     * Resolves an ActiveRecord by primary key
     *
     * @param recordType the ActiveRecord Class
     * @param key        the primary key of the record
     * @param <R>        the ActiveRecord Class
     * @return an ActiveRecord, or null if no match exists
     */
    <R extends ActiveRecord<R>> R resolveDimension(Class<R> recordType, Object key);

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
     * @param clazz the Arbiter class to retrieve the duration for
     * @return a duration in ms
     */
    <A extends Arbiter<?, ?>> long getRuleDuration(Class<A> clazz);

    /**
     * Starts timing rules processing for a given Arbiter class
     *
     * @param clazz the Arbiter class
     */
    <A extends Arbiter<?, ?>> void startRules(Class<A> clazz);

    /**
     * Finish timing rules processing for a given Arbiter class
     *
     * @param clazz   the Arbiter class
     * @param session the RulesSession that ran the rules
     */
    <A extends Arbiter<?, ?>> void finishRules(Class<A> clazz, RulesSession session);

    /**
     * Gets a count of inserted facts
     *
     * @return a count of inserted facts
     */
    int getFactCount();

    /**
     * Gets a count of facts inserted of a given Fact class
     *
     * @param clazz the Fact class counted
     * @return a count of the inserted facts
     */
    int getFactCount(Class<?> clazz);

    /**
     * Gets a total duration of fact gathering
     *
     * @return a duration in ms
     */
    long getFactDuration();

    /**
     * Gets the total duration of fact gathering for a given Fact class
     *
     * @param clazz the Fact class to retrieve the duration for
     * @return a duration in ms
     */
    long getFactDuration(Class<?> clazz);

    /**
     * Starts timing gathering for a given Fact class
     *
     * @param clazz the Fact class
     */
    void startFacts(Class<?> clazz);

    /**
     * Finish timing gathering for a given Fact class
     *
     * @param clazz the Fact class
     * @param count the total count of inserted facts
     */
    void finishFacts(Class<?> clazz, int count);

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
