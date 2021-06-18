package com.example.rules.spi;

import com.example.rules.api.FactStatistic;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleSession;

import java.util.Map;

public interface RuleStats {

    /**
     * Returns a Map of Rule IDs and their assertion count
     *
     * @return a Map of Rule IDs and their assertion count
     */
    Map<String, Integer> getRuleHistogram();

    /**
     * Returns a Map of fact classes and their count and duration
     *
     * @return a Map of fact classes and their count and duration
     */
    Map<String, FactStatistic> getFactStatistics();

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
}
