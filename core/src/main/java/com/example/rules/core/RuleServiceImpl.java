package com.example.rules.core;

import com.example.rules.api.*;
import com.example.rules.core.processor.ArbiterFactory;
import com.example.rules.core.context.RuleContextFactory;
import com.example.rules.core.domain.RuleLog;
import com.example.rules.core.repository.RuleLogRepository;
import com.example.rules.core.repository.RuleSerializer;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleCancellationEvent;
import com.example.rules.spi.store.ResultStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class RuleServiceImpl implements RuleService {

    private final ArbiterFactory arbiterFactory;
    private final RuleContextFactory ruleContextFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicLong idGenerator = new AtomicLong(1);

    private RuleLogRepository logRepository;
    private ResultStore resultStore;
    private AsyncTaskExecutor arbiterExecutor;

    public RuleServiceImpl(ArbiterFactory arbiterFactory, RuleContextFactory ruleContextFactory, ApplicationEventPublisher eventPublisher) {
        this.arbiterFactory = arbiterFactory;
        this.ruleContextFactory = ruleContextFactory;
        this.eventPublisher = eventPublisher;
    }

    @Autowired(required = false)
    public void setLogRepository(RuleLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Autowired(required = false)
    public void setResultStore(ResultStore resultStore) {
        this.resultStore = resultStore;
    }

    @Autowired
    @Qualifier("arbiterPool")
    public void setArbiterExecutor(AsyncTaskExecutor arbiterExecutor) {
        this.arbiterExecutor = arbiterExecutor;
    }

    @Override
    public Future<Serializable> submit(RuleRequest request) {
        long runId = onStart(request);
        return arbiterExecutor.submit(() -> run(request, runId));
    }

    @Override
    public long schedule(RuleRequest request) {
        long runId = onStart(request);
        arbiterExecutor.submit(() -> run(request, runId));
        return runId;
    }

    @Override
    public <T extends Serializable> T run(RuleRequest request) {
        long runId = onStart(request);
        return run(request, runId);
    }

    private <T extends Serializable> T run(RuleRequest request, long runId) {
        onRunning(runId);

        try {
            RuleContext context = ruleContextFactory.newContext(request, runId);
            Arbiter<RuleRequest, T> arbiter = arbiterFactory.getArbiter(context);
            T result = arbiter.processRules();
            onSuccess(runId, result);
            return result;
        } catch (Exception e) {
            onFailure(runId);
            return null;
        }
    }

    private long onStart(RuleRequest request) {
        if (logRepository != null) {
            RuleLog logEntry = new RuleLog();
            logEntry.setState(RuleRequest.State.PENDING);
            logEntry.setRequestClass(request.getClass().getName());
            logEntry.setRequestHash(request.hashCode());
            logEntry.setRequestData(RuleSerializer.serialize(request));
            logEntry.setRequestDescription(request.toString());
            return logRepository.save(logEntry).getId();
        } else {
            return idGenerator.getAndIncrement();
        }
    }

    private void onRunning(long runId) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState(RuleRequest.State.RUNNING);
                logRepository.save(log);
            });
        }
    }

    private void onSuccess(long runId, Serializable result) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState(RuleRequest.State.SUCCESS);
                log.setResultClass(result.getClass().getName());
                log.setResultDescription(result.toString());
                logRepository.save(log);
            });
        }
        if (resultStore != null) {
            resultStore.save(runId, result);
        }
    }

    private void onFailure(long runId) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState(RuleRequest.State.FAILURE);
                logRepository.save(log);
            });
        }
    }

    @Override
    public <T extends Serializable> T getResult(long ruleId) {
        return resultStore != null ? resultStore.load(ruleId) : null;
    }

    @Override
    public long findId(RuleRequest request) {
        if (logRepository != null) {
            return logRepository.findMostRecentRequest(request)
                    .map(RuleLog::getId)
                    .orElse(-1L);
        } else {
            return -1;
        }
    }

    @Override
    public Collection<Class<? extends RuleRequest>> getKnownRequests() {
        return new ArrayList<>(arbiterFactory.getKnownRequests());
    }

    @Override
    public Class<? extends Serializable> getResultClass(Class<? extends RuleRequest> requestClass) {
        return arbiterFactory.getResultClass(requestClass);
    }

    @Override
    public RuleRequest.State getState(long ruleId) {
        if (logRepository != null) {
            return logRepository.findById(ruleId).map(RuleLog::getState).orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public void cancel(long ruleId) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new RuleCancellationEvent(this, ruleId));
        }
    }

    @Override
    public Collection<RuleInfo> getRuleInfo() {
        return getKnownRequests().stream()
                .map(this::getRuleInfo)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RuleInfo> getRuleInfo(Class<? extends RuleRequest> requestClass) {
        // TODO Translate internal rules for info
        return null;
    }
}
