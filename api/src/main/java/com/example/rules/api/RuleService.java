package com.example.rules.api;

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
    Future<RuleResult> submit(RuleRequest request);

    /**
     * Executes a synchronous rule run against a given request
     *
     * @param request the RuleRequest
     * @return the RuleResult of the run
     */
    <T extends RuleResult> T run(RuleRequest request);

    /**
     * Returns the result of a rule run
     *
     * @param ruleId the ID of a rule run
     * @return the RuleResult of the action (which may be partially complete), or {@code null} if the run is either expired or never scheduled
     */
    <T extends RuleResult> T getResult(long ruleId);

    /**
     * Returns the ID of a rule run request previously executed on this request
     *
     * @param request the RuleRequest
     * @return the ID of the run, or -1 if the run is either expired or never scheduled
     */
    long findId(RuleRequest request);

    /**
     * Retrieves a Collection of known Request fully-qualified class names
     *
     * @return a Collection of known Request fully-qualified class names
     */
    Collection<String> getKnownRequests();

    /**
     * Gets the class of the RuleResult that would be returned for a given request
     *
     * @param request the RuleRequest
     * @return the class of the resulting RuleResult
     */
    Class<? extends RuleResult> getResultClass(RuleRequest request);

    /**
     * Gets the class of the RuleResult that would be returned for a given request class
     *
     * @param requestClass the RuleRequest class
     * @return the class of the resulting RuleResult
     */
    Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass);

//    /**
//     * Returns the state of a rule run
//     *
//     * @param ruleId the ID of a rule run
//     * @return the run state (INIT, RUNNING, SUCCESS, FAILURE), or null if the run is either expired or never scheduled
//     */
//    Action.State getStatus(String ruleId);

    /**
     * Cancels a rule run
     *
     * @param ruleId the ID of an in-progress rule run
     */
    void cancel(long ruleId);

    /**
     * Returns a Collection of RuleInfo for all of the rule objects known by this instance
     *
     * @return a Collection of RuleInfo
     */
    Collection<RuleInfo> getRuleInfo();

    /**
     * Returns a Collection of RuleInfo for all of the rule objects related to the given request class
     *
     * @return a Collection of RuleInfo
     */
    Collection<RuleInfo> getRuleInfo(Class<? extends RuleRequest> requestClass);
}
