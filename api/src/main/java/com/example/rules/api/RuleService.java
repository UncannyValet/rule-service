package com.example.rules.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * A service which provides business rule evaluation to an application
 */
public interface RuleService {

    /**
     * Schedules an asynchronous rule run against a given request
     *
     * @param request the RuleRequest
     * @return an ID used to track the run
     */
    long schedule(RuleRequest request);

    /**
     * Schedules an asynchronous rule run against a given request
     *
     * @param request the RuleRequest
     * @return a Future used to track the run
     */
    Future<Serializable> submit(RuleRequest request);

    /**
     * Executes a synchronous rule run against a given request
     *
     * @param request the RuleRequest
     * @return the result of the run
     */
    <T extends Serializable> T run(RuleRequest request);

    /**
     * Returns the result of a rule run
     *
     * @param ruleId the ID of a rule run
     * @return the result of the run, or {@code null} if the run is either expired, never scheduled or not persisted
     */
    <T extends Serializable> T getResult(long ruleId);

    /**
     * Returns the ID of a rule run request previously executed on this request
     *
     * @param request the RuleRequest
     * @return the ID of the run, or -1 if the run is either expired or never scheduled
     */
    long findId(RuleRequest request);

    /**
     * Returns the state of a rule run
     *
     * @param ruleId the ID of a rule run
     * @return the run state (RUNNING, SUCCESS, etc.), or null if the run is either expired or never scheduled
     */
    RuleRequest.State getState(long ruleId);

    /**
     * Cancels a rule run
     *
     * @param ruleId the ID of an in-progress rule run
     */
    void cancel(long ruleId);

    /**
     * Retrieves a Collection of known Request classes
     */
    Collection<Class<? extends RuleRequest>> getKnownRequests();

    /**
     * Gets the class of the result that would be returned for a given request class
     *
     * @param requestClass the RuleRequest class
     * @return the class of the result
     */
    Class<? extends Serializable> getResultClass(Class<? extends RuleRequest> requestClass);

    /**
     * Returns a Collection of RuleInfo for all the rule objects known by this instance
     */
    Collection<RuleInfo> getRuleInfo();

    /**
     * Returns a Collection of RuleInfo for all the rule objects related to the given request class
     */
    Collection<RuleInfo> getRuleInfo(Class<? extends RuleRequest> requestClass);
}
