package com.example.rules.spi.arbiter;

import com.example.rules.api.*;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.RuleStats;
import com.example.rules.spi.session.RuleCancellationEvent;
import com.example.rules.spi.session.RuleSession;
import com.example.rules.spi.utils.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract implementation of the Arbiter interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RuleRequest class associated with this Arbiter
 * @param <O> the result class associated with this Arbiter
 */
public abstract class AbstractArbiter<R extends RuleRequest, O extends Serializable> implements Arbiter<R, O> {

    private static final String[] EMPTY_RULE_SET = new String[]{};
    private static final Map<Class<?>, Class<?>> resultClasses = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String[]> ruleSetMap = new ConcurrentHashMap<>();

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final RuleContext context;
    private final String[] ruleSets;
    private final O result;

    private volatile RuleSession runningSession;

    public AbstractArbiter(RuleContext context) {
        this.context = context;

        // Set result, creating one if not available
        @SuppressWarnings("unchecked")
        Class<O> resultClass = (Class<O>)resultClasses.computeIfAbsent(getClass(), clazz -> ClassUtils.getTypeArgument(clazz, Arbiter.class, 1));
        if (resultClass == null) {
            throw new RuleException("Failed to instantiate result object");
        }
        Serializable r = context.getResult();
        if (r == null) {
            result = ClassUtils.instantiate(resultClass);
        } else {
            // Copy the result using serialization
            // Intermediate updates by rules must not interfere with API calls to retrieve the result
            result = resultClass.cast(SerializationUtils.clone(r));
        }

        ruleSets = ruleSetMap.computeIfAbsent(getClass(), clazz -> {
            RuleSet annotation = getClass().getAnnotation(RuleSet.class);
            return annotation != null ? annotation.value() : EMPTY_RULE_SET;
        });
    }

    protected final RuleContext getContext() {
        return context;
    }

    /**
     * Gets the result this processor was initialized with (or created)
     *
     * @return the result
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
            throw new RuleException("Failed to process request " + context.getRequest().getClass().getSimpleName(), e);
        } finally {
            LOG.debug(context.getId() + " finished");
        }
        context.setResult(result);
        return result;
    }

    private void runSession() {
        try (RuleSession session = context.newSession(ruleSets)) {
            // Don't bother running if no rules are defined for the session
            int totalRules = session.getRuleCount();
            if (totalRules > 0) {
                RuleStats statistics = context.getStats();
                session.setLogger(LOG);
                beforeFacts(session);
                LOG.info("Gathering facts...");
                context.investigate(session);
                LOG.info("Fact gathering complete");
                statistics.getFactStatistics().forEach((type, stats) -> LOG.info("- " + type + ": " + stats.getCount() + " fact(s) in " + stats.getDuration() + " ms"));
                beforeRules(session);
                LOG.info("Running " + totalRules + " rule(s) over " + session.getFactCount() + " fact(s)...");
                statistics.startRules(getClass());
                runningSession = session;
                int ruleCount;
                try {
                    ruleCount = session.runRules();
                } finally {
                    runningSession = null;
                }
                statistics.finishRules(getClass(), session);
                LOG.info("Rule session complete, " + ruleCount + " rule(s) asserted in " + statistics.getRuleDuration(this.getClass()) + " ms");
                afterRules(session);
            }
        }
    }

    /**
     * Listener to process session cancellations, stopping the rule session if it is in progress
     */
    @EventListener(RuleCancellationEvent.class)
    public void onCancellationEvent(RuleCancellationEvent event) {
        RuleSession session = runningSession;
        if (session != null && context.getId() == event.getSessionId()) {
            session.halt();
        }
    }

    /**
     * Override in subclasses to execute code before the RulesSession is opened
     */
    protected void beforeSession() {
    }

    /**
     * Override in subclasses to execute code before facts are inserted into the RulesSession
     *
     * @param session the active RuleSession
     */
    protected void beforeFacts(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code before rules are executed
     *
     * @param session the active RuleSession
     */
    protected void beforeRules(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code after rules have completed
     *
     * @param session the active RuleSession
     */
    protected void afterRules(RuleSession session) {
    }

    /**
     * Override in subclasses to execute code after the RuleSession is closed
     */
    protected void afterSession() {
    }
}
