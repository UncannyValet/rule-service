package com.example.rules.core.context;

import com.example.rules.api.*;
import com.example.rules.core.investigator.InvestigatorFactory;
import com.example.rules.core.session.SessionFactory;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.session.RuleCancellationEvent;
import com.example.rules.spi.session.RuleSession;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static com.example.rules.api.ErrorNumbers.INVESTIGATOR_FAILURE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RuleContextImpl implements RuleContext {

    @SuppressWarnings("unchecked")
    private static final CompletableFuture<Investigator<?, ?>>[] FUTURE_ARRAY = new CompletableFuture[]{};

    private final long id;
    private final RuleRequest request;
    private final SessionFactory sessionFactory;
    private final InvestigatorFactory investigatorFactory;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<Investigator<?, ?>, CompletableFuture<Investigator<?, ?>>> running = new IdentityHashMap<>();

    private RuleResult result;
    private boolean stopped;

    public RuleContextImpl(long id, RuleRequest request, SessionFactory sessionFactory, InvestigatorFactory investigatorFactory) {
        this.id = id;
        this.request = request;
        this.sessionFactory = sessionFactory;
        this.investigatorFactory = investigatorFactory;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RuleRequest> T getRequest() {
        return (T)request;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RuleResult> T getResult() {
        return (T)result;
    }

    @Override
    public void setResult(RuleResult result) {
        this.result = result;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    @Override
    public <R> R resolveDimension(Class<R> dimensionClass, Object id) {
        return null;
    }

    @Override
    public RuleSession newSession(String... sessionIds) {
        return sessionFactory.getSession(request, sessionIds);
    }

    @Override
    public void investigate(RuleSession session) {
        Set<Investigator<?, ?>> waiting = new HashSet<>(investigatorFactory.getInvestigators(this));

        // Run investigators, delaying those with dependencies until the dependencies have completed
        // TODO probably doesn't execute the number of times it should...
        while (!waiting.isEmpty()) {
            synchronized (running) {
                waiting.stream()
                        .filter(i -> !running.containsKey(i))
                        .filter(i -> !i.dependsOn(waiting))
                        .forEach(i -> running.put(i, schedule(i, session)));
            }

            try {
                Investigator<?, ?> o = (Investigator<?, ?>)CompletableFuture.anyOf(running.values().toArray(FUTURE_ARRAY)).get();
                running.remove(o);
            } catch (InterruptedException e) {
                if (isStopped()) {
                    // Request cancelled, cancel spawned investigators as well
                    running.values().forEach(f -> f.cancel(true));
                } else {
                    throw new RuleException(e, INVESTIGATOR_FAILURE);
                }
            } catch (ExecutionException e) {
                throw new RuleException(e, INVESTIGATOR_FAILURE);
            }
        }
    }

    private CompletableFuture<Investigator<?, ?>> schedule(Investigator<?, ?> investigator, RuleSession session) {
        return CompletableFuture.supplyAsync(() -> {
            investigator.gatherFacts(session);
            return investigator;
        });
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public Map<String, Integer> getRuleStatistics() {
        return null;
    }

    @Override
    public Map<String, FactStatistic> getFactStatistics() {
        return null;
    }

    @Override
    public int getRuleCount() {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> int getRuleCount(Class<A> clazz) {
        return 0;
    }

    @Override
    public long getRuleDuration() {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> long getRuleDuration(Class<A> arbiterClass) {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> void startRules(Class<A> arbiterClass) {

    }

    @Override
    public <A extends Arbiter<?, ?>> void finishRules(Class<A> arbiterClass, RuleSession session) {

    }

    @Override
    public int getFactCount() {
        return 0;
    }

    @Override
    public int getFactCount(Class<?> factClass) {
        return 0;
    }

    @Override
    public long getFactDuration() {
        return 0;
    }

    @Override
    public long getFactDuration(Class<?> factClass) {
        return 0;
    }

    @Override
    public void startFacts(Class<?> factClass) {

    }

    @Override
    public void finishFacts(Class<?> factClass, int count) {

    }

    @EventListener(RuleCancellationEvent.class)
    public void onCancellationEvent(RuleCancellationEvent event) {
        if (id == event.getSessionId()) {
            // TODO cancel running investigators
        }
    }
}
