package com.example.rules.spi.session;

import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides access to a set of rules, including insertion of facts, global access, and rule execution
 */
public interface RuleSession extends AutoCloseable {

    /**
     * Inserts a Fact into the session's working memory
     *
     * @param fact the Fact to insert
     */
    <F> void insert(F fact);

    /**
     * Retrieves a Collection of Facts present in this session
     *
     * @return a Collection of Facts
     */
    default Collection<Object> getFacts() {
        return getFacts(Object.class);
    }

    /**
     * Retrieves a Collection of Facts of the given class present in this session
     *
     * @return a Collection of Facts of the given class
     */
    default <T> Collection<T> getFacts(Class<T> factClass) {
        return getFactStream(factClass).collect(Collectors.toList());
    }

    /**
     * Retrieves a Stream of Facts of the given class present in this session
     *
     * @return a Collection of Facts of the given class
     */
    <T> Stream<T> getFactStream(Class<T> factClass);

    /**
     * Sets the value of a global variable in the session
     *
     * @param identifier the ID of the variable
     * @param value      the value to set
     */
    void setGlobal(String identifier, Object value);

    /**
     * Retrieves the value of a global variable from the session
     *
     * @param identifier the ID of the variable
     * @return the value of the variable
     */
    <T> T getGlobal(String identifier);

    /**
     * Runs the rules associated with the session against the inserted facts
     *
     * @return the number of rules asserted
     */
    int runRules();

    /**
     * Stops processing of rules
     */
    void halt();

    /**
     * Retrieves the count of rules attached to this session
     *
     * @return the count of rules associated with this session
     */
    int getRuleCount();

    /**
     * Retrieves the count of facts attached to this session
     *
     * @return the count of facts attached to this session
     */
    default long getFactCount() {
        return getFactStream(Object.class).count();
    }

    /**
     * Externally assign a logger to be used by this session
     *
     * @param log the ILogger to use for the session
     */
    void setLogger(Logger log);

    /**
     * Returns a Map of Rule IDs with a count of the times each was triggered
     *
     * @return a Map of Rule IDs with a count of the times each was triggered
     */
    Map<String, Integer> getRuleHistogram();

    /**
     * Adds an {@link EventListener} to the session
     * <p/>This listener's use (how or if) is specific to the type of session
     */
    default <E extends EventListener> void addEventListener(E listener) {
    }

    @Override
    default void close() {
    }
}
