package com.example.rules.spi;

import com.spirent.cem.rules.api.FactStatistic;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;

import java.util.Map;

public interface RulesContext {

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
    <T extends RulesRequest> T getRequest();

    /**
     * Retrieves the current RulesResult, can be null if no result has been set yet
     *
     * @return the current RulesResult
     */
    <T extends RulesResult> T getResult();

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
    Map<String, Integer> getRuleHistogram();

    /**
     * Returns a Map of fact classes and their count and duration
     *
     * @return a Map of fact classes and their count and duration
     */
    Map<String, FactStatistic> getFactStatistics();
}
