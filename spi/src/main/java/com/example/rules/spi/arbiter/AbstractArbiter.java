package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.Context;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.investigator.InvestigatorFactory;
import com.example.rules.spi.session.RuleCancellationEvent;
import com.example.rules.spi.session.RuleSession;
import com.example.rules.spi.session.SessionFactory;
import com.example.rules.spi.utils.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.example.rules.api.ErrorNumbers.INVESTIGATOR_FAILURE;
import static com.example.rules.api.ErrorNumbers.PROCESS_FAILURE;

/**
 * An abstract implementation of the Arbiter interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RuleRequest class associated with this Arbiter
 * @param <O> the RuleResult class associated with this Arbiter
 */
public abstract class AbstractArbiter<R extends RuleRequest, O extends RuleResult> implements Arbiter<R, O> {

    private static final String[] EMPTY_RULE_SET = new String[]{};
    private static final Map<Class<?>, Class<?>> resultClasses = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String[]> ruleSetMap = new ConcurrentHashMap<>();

    protected final Logger LOG = LogManager.getLogger(getClass());

    private final Context context;
    private final InvestigatorFactory investigatorFactory;
    private final SessionFactory sessionFactory;
    private final String[] ruleSets;

    private final O result;

    private volatile RuleSession runningSession;

    public AbstractArbiter(Context context, InvestigatorFactory investigatorFactory, SessionFactory sessionFactory) {
        this.context = context;
        this.investigatorFactory = investigatorFactory;
        this.sessionFactory = sessionFactory;

        // Set result, creating one if not available
        RuleResult r = context.getResult();
        if (r == null) {
            @SuppressWarnings("unchecked")
            Class<O> resultClass = (Class<O>)resultClasses.computeIfAbsent(getClass(), clazz -> ClassUtils.getTypeArgument(clazz, Arbiter.class, 1));
            result = ClassUtils.instantiate(resultClass);
        } else {
            // Copy the result using serialization
            // Intermediate updates by rules must not interfere with API calls to retrieve the result
            result = (O)SerializationUtils.clone(r);
        }

        ruleSets = ruleSetMap.computeIfAbsent(getClass(), clazz -> {
            RuleSet annotation = getClass().getAnnotation(RuleSet.class);
            return annotation != null ? annotation.value() : EMPTY_RULE_SET;
        });
    }

    protected final Context getContext() {
        return context;
    }

    /**
     * Gets the RulesResult this processor was initialized with (or created)
     *
     * @return the RulesResult
     */
    protected final O getResult() {
        return result;
    }

    @Override
    public final O processRules() {
        try {
            beforeSession();
            runSession();
            afterSession();
        } catch (CancellationException e) {
            // Run was cancelled, propagate up
            LOG.debug(context.getId() + " was cancelled", e);
            throw e;
        } catch (Throwable e) {
            LOG.debug(context.getId() + " terminated by an exception", e);
            if (context.isStopped() /*&& ExceptionUtilities.containsTypeOf(e, InterruptedException.class)*/) {
                // Session cancelled, here or in Investigator
                throw new CancellationException();
            }
            throw new RuleException(e, PROCESS_FAILURE, context.getRequest().getName());
        } finally {
            LOG.debug(context.getId() + " finished");
        }
        context.setResult(result);
        return result;
    }

    @SuppressWarnings("squid:S00112")
    private void runSession() {
        try (RuleSession session = sessionFactory.getSession(context.getRequest(), ruleSets)) {
            // Don't bother running if no rules are defined for the session
            int totalRules = session.getRuleCount();
            if (totalRules > 0) {
                session.setLogger(LOG);
                beforeFacts(session);
                LOG.info("Gathering facts...");
                investigate(session);
                beforeRules(session);
                LOG.info("Fact gathering complete");
                context.getFactStatistics().forEach((type, stats) -> LOG.info("- " + type + ": " + stats.getCount() + " fact(s) in " + stats.getDuration() + " ms"));
                LOG.info("Running " + totalRules + " rule(s) over " + session.getFactCount() + " fact(s)...");
                context.startRules(getClass());
                runningSession = session;
                int ruleCount;
                try {
                    ruleCount = session.runRules();
                } finally {
                    runningSession = null;
                }
                context.finishRules(getClass(), session);
                LOG.info("Rule session complete, " + ruleCount + " rule(s) asserted in " + context.getRuleDuration(this.getClass()) + " ms");
                afterRules(session);
            }
        }
    }

    /**
     * Spawns Investigators in parallel to gather facts, accounting for dependencies if any exist
     */
    private void investigate(RuleSession session) {
        Set<Investigator<R, ?>> currentInvestigators = new HashSet<>(investigatorFactory.getInvestigators(context));

        // Run investigators, delaying those with dependencies until the dependencies have completed
        while (!currentInvestigators.isEmpty()) {
            List<Future<Investigator<R, ?>>> futures = currentInvestigators.stream()
                    .filter(i -> !i.dependsOn(currentInvestigators))
                    .map(i -> context.scheduleInvestigation(i, session))
                    .collect(Collectors.toList());

            futures.forEach(future -> {
                try {
                    currentInvestigators.remove(future.get());
                } catch (InterruptedException e) {
                    if (context.isStopped()) {
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

    /**
     * Listener to process session cancellations, stopping the rule session if it is in progress
     */
    @EventListener(RuleCancellationEvent.class)
    public void onCancellationEvent(RuleCancellationEvent event) {
        RuleSession session = runningSession;
        if (session != null && context.getId().equals(event.getSessionId())) {
            session.halt();
        }
    }

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
     * Override in subclasses to execute code before rules are executed
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
