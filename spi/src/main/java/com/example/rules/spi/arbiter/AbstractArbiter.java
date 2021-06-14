package com.example.rules.spi.arbiter;

//import com.daxtechnologies.exception.ExceptionUtilities;
//import com.daxtechnologies.oam.ILogger;
//import com.daxtechnologies.oam.TheLogger;
//import com.daxtechnologies.services.Provider;
//import com.daxtechnologies.services.trace.Trace;
//import com.daxtechnologies.util.ClassUtils;
//import com.daxtechnologies.util.Releasable;
//import com.daxtechnologies.util.StringUtils;
import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.Context;
import com.example.rules.spi.RulesService;
import com.example.rules.spi.investigator.Investigator;
//import com.example.rules.spi.processor.AbstractRulesProcessor;
import com.example.rules.spi.session.RuleSession;
import com.example.rules.spi.utils.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.example.rules.api.ErrorNumbers.*;

/**
 * An abstract implementation of the Arbiter interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RuleRequest class associated with this Arbiter
 * @param <O> the RuleResult class associated with this Arbiter
 */
//@Provider
public abstract class AbstractArbiter<R extends RuleRequest, O extends RuleResult> /*extends AbstractRulesProcessor<R>*/ implements Arbiter<R, O> {

    protected final Logger LOG = LogManager.getLogger(getClass());

    private List<Investigator<R, ?>> investigators;
    private final List<RuleRequest> extensions = new ArrayList<>();

    private final RulesService rulesService = RulesService.getInstance();
    private O result;
    private final Set<String> ruleSet = new HashSet<>();
    private Class<O> resultClass;
//    private boolean parallelFacts;

//    private volatile RuleSession runningSession;

    @Override
    public void initialize(R request, Context context, RuleSession session, List<Investigator<R, ?>> investigators) {
//        super.doInitialize(objects);

//        R request = getRequest();
//        LOG.info("Initializing " + getName() + " to handle " + request.getName());

        // Set result, creating one if not available
        RuleResult r = context.getResult();
        this.investigators = investigators;
        if (r == null) {
            result = newResult();
        } else {
            // Copy the result using serialization
            // Result updates by rules must not interfere with API calls to retrieve the result
            result = resultClass.cast(SerializationUtils.clone(r));
        }

        // Retrieve and initialize appropriate investigators based on the request
        investigators.forEach(i -> i.initialize(request, context, session));
//                    LOG.info("- " + i.getName() + " gathers " + i.getFactClass().getSimpleName() + "(s)");

//        parallelFacts = config.getBoolean("request." + getRequestClass().getSimpleName() + ".parallel.facts", true);
    }

//    @Override
//    public void release() {
//        super.release();
//
//        investigators.forEach(Releasable::release);
//    }

//    @Override
//    public final void setOptions(ArbiterFactory<R, O, ? extends Arbiter<R, O>> factory/*, ArbiterOptions options*/) {
//        setRequestClass(factory.getRequestClass());
//        resultClass = factory.getResultClass();
        // Use the sessions registered with the service and from the arbiter annotation
//        ruleSet.addAll(rulesService.getRegisteredSessions(getRequestClass()));
//        if (options != null && StringUtils.isNotEmpty(options.rules())) {
//            ruleSet.add(options.rules());
//        }
//    }

//    @Override
//    public final Class<O> getResultClass() {
//        return resultClass;
//    }

    /**
     * Gets the RulesResult this processor was initialized with (or created)
     *
     * @return the RulesResult
     */
    protected final O getResult() {
        return result;
    }

    @Override
    public final Void call() {
        runRules();
        return null;
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public final Collection<RuleRequest> runRules() {
//        Context context = getContext();
//        String logPrefix = "Arbiter '" + getName() + ":" + getContext().getId() + "'";
//        LOG.info("Processing " + getRequest().getName() + " in session " + context.getId() + "...");
        try {
//            checkInterrupt();
//            Trace.doWithTask("Pre-Session", v -> beforeSession());
//            do {
            beforeSession();
                runSession();
                afterSession();
//            } while (continueBatch());
//            checkInterrupt();
//            Trace.doWithTask("Post-Session", v -> afterSession());
//            checkInterrupt();
        } catch (CancellationException e) {
            // Run was cancelled, propagate up
            LOG.debug(logPrefix + " was cancelled", e);
            throw e;
        } catch (Throwable e) {
            LOG.debug(logPrefix + " terminated by an exception", e);
            if (getContext().isStopped() && ExceptionUtilities.containsTypeOf(e, InterruptedException.class)) {
                // Session cancelled, here or in Investigator
                throw new CancellationException();
            }
            throw new RuleException(e, PROCESS_FAILURE, getRequest().getName());
        } finally {
            LOG.debug(logPrefix + " finished");
        }
        context.setResult(result);
        return extensions;
    }

    @SuppressWarnings("squid:S00112")
    private void runSession() {
        try (RuleSession session = rulesService.getSession(ruleSet)) {
            // Don't bother running if no rules are defined for the session
            int totalRules = session.getRuleCount();
            if (totalRules > 0) {
//                Context context = getContext();
                session.setLogger(LOG);
//                checkInterrupt();
//                Trace.doWithTask("Pre-Facts", v -> beforeFacts(session));
                beforeFacts(session);
                // Gather facts for each fact type using registered investigators
                LOG.info("Gathering facts...");
//                checkInterrupt();
//                Trace.doWithTask("Investigation", v -> {
                    if (parallelFacts && investigators.size() > 1) {
                        investigate(session);
                    } else {
                        investigators.forEach(investigator -> {
                            investigator.setSession(session);
                            investigator.run();
                        });
                    }
//                });
//                checkInterrupt();
//                Trace.doWithTask("Pre-Rules", v -> beforeRules(session));
                beforeRules(session);
//                checkInterrupt();
                LOG.info("Fact gathering complete");
                context.getFactStatistics().forEach((type, stats) -> LOG.info("- " + type + ": " + stats.getCount() + " fact(s) in " + stats.getDuration() + " ms"));
                LOG.info("Running " + totalRules + " rule(s) over " + session.getFactCount() + " fact(s)...");
                context.startRules(getClass());
                runningSession = session;
                Trace.doWithTask("Rule Session " + ruleSet, v -> {
                    int ruleCount = session.runRules();
                    runningSession = null;
                    context.finishRules(getClass(), session);
                    LOG.info("Rule session " + ruleSet + " complete, " + ruleCount + " rule(s) asserted in " + context.getRuleDuration(this.getClass()) + " ms");
                });
                checkInterrupt();
                Trace.doWithTask("Post-Rules", v -> afterRules(session));
            }
        }
    }

    /**
     * Spawns Investigators in parallel to gather facts, accounting for dependencies if any exist
     *
     * @param session the RulesSession into which facts are inserted
     */
    private void investigate(RuleSession session) {
        Set<Investigator<R, ?>> currentInvestigators = new HashSet<>(investigators);

        // Run investigators, delaying those with dependencies until the dependencies have completed
        while (!currentInvestigators.isEmpty()) {
            List<Future<Investigator<R, ?>>> futures = currentInvestigators.stream()
                    .filter(i -> !i.dependsOn(currentInvestigators))
                    .peek(i -> i.setSession(session))
                    .peek(i -> i.setTrace(Trace.getCurrent()))
                    .map(rulesService::scheduleInvestigation)
                    .collect(Collectors.toList());

            futures.forEach(future -> {
                try {
                    currentInvestigators.remove(future.get());
                } catch (InterruptedException e) {
                    if (getContext().isStopped()) {
                        // Request cancelled, cancel spawned investigators as well
                        futures.forEach(f -> f.cancel(true));
                        Thread.currentThread().interrupt();
                    } else {
                        throw new RuleException(e, INVESTIGATOR_FAILURE);
                    }
                } catch (ExecutionException e) {
                    throw new RuleException(e, INVESTIGATOR_FAILURE);
                }
            });
        }
    }

//    /**
//     * Returns true when the Investigators used by this Arbiter have more data to provide for a batch operation
//     *
//     * @return {@code true} if all the Investigators have more data to provide, {@code false} otherwise
//     */
//    private boolean continueBatch() {
//        return investigators.stream()
//                .map(Investigator::dataAvailable)
//                .reduce((a, b) -> a && b)
//                .orElse(false);
//    }

    /**
     * Creates a new RulesResult
     *
     * @return a new RulesResult
     */
    private O newResult() {
        return ClassUtils.instantiate(resultClass);
    }

//    /**
//     * Called in subclasses to register new requests to be run after this rules session completes
//     * <p>Can be called at any time before processing completes</p>
//     *
//     * @param request the new RulesRequest to run
//     */
//    protected final void registerExtension(RuleRequest request) {
//        extensions.add(request);
//    }

//    @Override
//    protected void doStop() {
//        super.doStop();
//
//        RulesSession currentSession = runningSession;
//        if (currentSession != null) {
//            currentSession.halt();
//        }
//    }

//    private static void checkInterrupt() {
//        if (Thread.interrupted()) {
//            throw new CancellationException();
//        }
//    }

    /**
     * Override in subclasses to execute code before the RulesSession is opened
     */
    protected void beforeSession() {
    }

    /**
     * Override in subclasses to execute code before facts are inserted into the RulesSession
     *
     * @param session the active RulesSession
     */
    protected void beforeFacts(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code before rules executed
     *
     * @param session the active RulesSession
     */
    protected void beforeRules(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code after rules have completed
     *
     * @param session the active RulesSession
     */
    protected void afterRules(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code after the RulesSession is closed
     */
    protected void afterSession() {
    }
}
