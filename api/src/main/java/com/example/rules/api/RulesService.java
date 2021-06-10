package com.example.rules.api;

//import com.daxtechnologies.services.*;
//import com.daxtechnologies.services.action.Action;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * A service which provides business rule evaluation to an application
 */
public interface RulesService /*extends Service*/ {

//    /**
//     * Gets a local instance of the RulesService
//     *
//     * @return a local instance of the RulesService
//     */
//    static RulesService getInstance() {
//        return Services.lookupService(RulesService.class);
//    }

//    /**
//     * Gets a remote instance of the RulesService running on a "rule.engine" process
//     *
//     * @return a remote instance of the RulesService
//     */
//    @SuppressWarnings("unused")
//    static RulesService getRemoteInstance() {
//        ServiceLookupContext context = ServiceLookupContext.newContext().processType("rule.engine");
//        return RulesService.getRemoteInstance(context);
//    }

//    /**
//     * Gets a remote instance of the RulesService matching a given context
//     *
//     * @return a remote instance of the RulesService
//     */
//    static RulesService getRemoteInstance(ServiceLookupContext context) {
//        return Services.lookupRemoteService(RulesService.class, context);
//    }

    /**
     * Schedules an asynchronous rules run against a given request
     *
     * @param request the RulesRequest
     * @return a Future used to track the run
     */
//    @Callable
    Future<RulesResult> schedule(RulesRequest request);

    /**
     * Executes a synchronous rules run against a given request
     *
     * @param request the RulesRequest
     * @return the RulesResult of the run
     */
    <T extends RulesResult> T run(RulesRequest request);

    /**
     * Executes a synchronous rules run against a given request, using a provided ID
     *
     * @param id      a manually provided ID to track the run, must be unique
     * @param request the RulesRequest
     * @return the RulesResult of the run
     */
    <T extends RulesResult> T run(String id, RulesRequest request);

    /**
     * Retrieves a Collection of known Request fully-qualified class names
     *
     * @return a Collection of known Request fully-qualified class names
     */
//    @Callable
    Collection<String> getKnownRequests();

    /**
     * Gets the class of the RulesResult that would be returned for a given request
     *
     * @param request the RulesRequest
     * @return the class of the resulting RulesResult
     */
//    @Callable
    Class<? extends RulesResult> getResultClass(RulesRequest request);

    /**
     * Gets the class of the RulesResult that would be returned for a given request class
     *
     * @param requestClass the RulesRequest class
     * @return the class of the resulting RulesResult
     */
//    @Callable
    Class<? extends RulesResult> getResultClass(Class<? extends RulesRequest> requestClass);

//    /**
//     * Returns the state of a rules run
//     *
//     * @param ruleId the ID of a rules run
//     * @return the run state (INIT, RUNNING, SUCCESS, FAILURE), or null if the run is either expired or never scheduled
//     */
//    @Callable
//    Action.State getStatus(String ruleId);

    /**
     * Returns the result of a rules run
     *
     * @param ruleId the ID of a rules run
     * @return the RulesResult of the action (which may be partially complete), or {@code null} if the run is either expired or never scheduled
     */
//    @Callable
    <T extends RulesResult> T getResult(String ruleId);

    /**
     * Cancels a rules run, forwarding across cluster if necessary
     *
     * @param ruleId the ID of an in-progress rules run
     * @return {@code true} if a local run was found and cancelled, {@code false} otherwise
     */
//    @Callable
    boolean cancel(String ruleId);

    /**
     * Returns the ID of a rules run request previously executed on this request
     *
     * @param request the RulesRequest
     * @return the ID of the run, or null if the run is either expired or never scheduled
     */
//    @Callable
    String getRulesId(RulesRequest request);

    /**
     * Returns a Collection of RuleInfo for all of the rules objects known by this instance
     *
     * @return a Collection of RuleInfo
     */
//    @Callable
    Collection<RuleInfo> getRuleInfo();

    /**
     * Returns a Collection of RuleInfo for all of the rules objects related to the given request class
     *
     * @return a Collection of RuleInfo
     */
//    @Callable
    Collection<RuleInfo> getRuleInfo(Class<? extends RulesRequest> requestClass);
}
