package com.example.rules.core.context;

import com.example.rules.api.*;
import com.example.rules.core.processor.InvestigatorFactory;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.RuleStats;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.session.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RuleContextImpl implements RuleContext {

    @SuppressWarnings("unchecked")
    private static final CompletableFuture<Investigator<?, ?>>[] FUTURE_ARRAY = new CompletableFuture[]{};

    private final long id;
    private final RuleRequest request;

    private SessionFactory sessionFactory;
    private InvestigatorFactory investigatorFactory;
    private AsyncTaskExecutor executor;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<Investigator<?, ?>, CompletableFuture<Investigator<?, ?>>> running = new IdentityHashMap<>();
    private final RuleStats statistics = new RuleStatsImpl();

    private Serializable result;
    private boolean stopped;

    public RuleContextImpl(long id, RuleRequest request) {
        this.id = id;
        this.request = request;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Autowired
    public void setInvestigatorFactory(InvestigatorFactory investigatorFactory) {
        this.investigatorFactory = investigatorFactory;
    }

    @Autowired(required = false)
    @Qualifier("investigatorPool")
    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
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
    public <T extends Serializable> T getResult() {
        return (T)result;
    }

    @Override
    public void setResult(Serializable result) {
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
    public RuleStats getStats() {
        return statistics;
    }

    @Override
    public RuleSession newSession(String... sessionIds) {
        return sessionFactory.getSession(request, sessionIds);
    }

    @Override
    public void investigate(RuleSession session) {
        Set<Investigator<?, ?>> waiting = new HashSet<>(investigatorFactory.getInvestigators(this));

        // Run investigators, delaying those with dependencies until the dependencies have completed
        while (!waiting.isEmpty()) {
            CompletableFuture<Investigator<?, ?>>[] futures;
            synchronized (running) {
                waiting.stream()
                        .filter(i -> !running.containsKey(i))
                        .filter(i -> !i.dependsOn(waiting))
                        .forEach(i -> running.put(i, schedule(i, session)));
                futures = running.values().toArray(FUTURE_ARRAY);
            }

            try {
                Investigator<?, ?> o = (Investigator<?, ?>)CompletableFuture.anyOf(futures).get();
                synchronized (running) {
                    running.remove(o);
                    waiting.remove(o);
                }
            } catch (InterruptedException | CancellationException e) {
                // Request cancelled, cancel spawned investigators as well
                running.values().forEach(f -> f.cancel(true));
            } catch (ExecutionException e) {
                throw new RuleException("Investigation failure", e);
            }
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    private CompletableFuture<Investigator<?, ?>> schedule(Investigator<?, ?> investigator, RuleSession session) {
        if (executor != null) {
            return CompletableFuture.supplyAsync(() -> {
                investigator.gatherFacts(session);
                return investigator;
            }, executor);
        } else {
            return CompletableFuture.completedFuture(investigator).thenApply(i -> {
                i.gatherFacts(session);
                return i;
            });
        }
    }

    @EventListener(RuleCancellationEvent.class)
    public void onCancellationEvent(RuleCancellationEvent event) {
        if (id == event.getSessionId()) {
            stopped = true;
            synchronized (running) {
                running.values().forEach(f -> f.cancel(true));
            }
        }
    }
}
