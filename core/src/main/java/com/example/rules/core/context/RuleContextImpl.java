package com.example.rules.core.context;

import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.core.investigator.InvestigatorFactory;
import com.example.rules.core.session.SessionFactory;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.RuleStats;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.session.RuleCancellationEvent;
import com.example.rules.spi.session.RuleSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

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

    private AsyncTaskExecutor executor;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<Investigator<?, ?>, CompletableFuture<Investigator<?, ?>>> running = new IdentityHashMap<>();
    private final RuleStats statistics = new RuleStatsImpl();

    private RuleResult result;

    public RuleContextImpl(long id, RuleRequest request, SessionFactory sessionFactory, InvestigatorFactory investigatorFactory) {
        this.id = id;
        this.request = request;
        this.sessionFactory = sessionFactory;
        this.investigatorFactory = investigatorFactory;
    }

    @Autowired
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
                throw new RuleException(e, INVESTIGATOR_FAILURE);
            }
        }
    }

    private CompletableFuture<Investigator<?, ?>> schedule(Investigator<?, ?> investigator, RuleSession session) {
        return CompletableFuture.supplyAsync(() -> {
            investigator.gatherFacts(session);
            return investigator;
        }, executor);
    }

    @EventListener(RuleCancellationEvent.class)
    public void onCancellationEvent(RuleCancellationEvent event) {
        if (id == event.getSessionId()) {
            synchronized (running) {
                running.values().forEach(f -> f.cancel(true));
            }
        }
    }
}
