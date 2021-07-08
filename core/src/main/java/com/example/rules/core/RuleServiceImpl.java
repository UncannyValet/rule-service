package com.example.rules.core;

import com.example.rules.api.*;
import com.example.rules.core.arbiter.ArbiterFactory;
import com.example.rules.core.context.RuleContextFactory;
import com.example.rules.core.model.RuleLog;
import com.example.rules.core.repository.RuleLogRepository;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleCancellationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class RuleServiceImpl implements RuleService {

    private final ArbiterFactory arbiterFactory;
    private final RuleContextFactory ruleContextFactory;
    private final AtomicLong idGenerator = new AtomicLong(1);

    private RuleLogRepository logRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private AsyncTaskExecutor arbiterExecutor;

    public RuleServiceImpl(ArbiterFactory arbiterFactory, RuleContextFactory ruleContextFactory) {
        this.arbiterFactory = arbiterFactory;
        this.ruleContextFactory = ruleContextFactory;
    }

    @Autowired(required = false)
    public void setLogRepository(RuleLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    @Qualifier("arbiterPool")
    public void setArbiterExecutor(AsyncTaskExecutor arbiterExecutor) {
        this.arbiterExecutor = arbiterExecutor;
    }

    @Override
    public Future<RuleResult> submit(RuleRequest request) {
        long runId = createLog(request);
        return arbiterExecutor.submit(() -> run(request, runId));
    }

    @Override
    public long schedule(RuleRequest request) {
        long runId = createLog(request);
        arbiterExecutor.submit(() -> run(request, runId));
        return runId;
    }

    @Override
    public <T extends RuleResult> T run(RuleRequest request) {
        long runId = createLog(request);
        return run(request, runId);
    }

    private <T extends RuleResult> T run(RuleRequest request, long runId) {
        logRunning(runId);

        try {
            RuleContext context = ruleContextFactory.newContext(request, runId);
            Arbiter<RuleRequest, T> arbiter = arbiterFactory.getArbiter(context);
            T result = arbiter.processRules();
            logSuccess(runId, result);
            return result;
        } catch (Exception e) {
            logFailure(runId);
            return null;
        }
    }

    private long createLog(RuleRequest request) {
        if (logRepository != null) {
            RuleLog logEntry = new RuleLog();
            logEntry.setState("CREATED");
            logEntry.setRequestClass(request.getClass().getName());
            logEntry.setRequestHash(request.hashCode());
            logEntry.setRequestData(RuleSerializer.serialize(request));
            logEntry.setRequestDescription(request.getDescription());
            return logRepository.save(logEntry).getId();
        } else {
            return idGenerator.getAndIncrement();
        }
    }

    private void logRunning(long runId) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState("RUNNING");
                logRepository.save(log);
            });
        }
    }

    private void logSuccess(long runId, RuleResult result) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState("SUCCESS");
                log.setResultClass(result.getClass().getName());
                log.setResultDescription(result.getDescription());
                logRepository.save(log);
            });
        }
    }

    private void logFailure(long runId) {
        if (logRepository != null) {
            logRepository.findById(runId).ifPresent(log -> {
                log.setUpdateTime(LocalDateTime.now());
                log.setState("FAILURE");
                logRepository.save(log);
            });
        }
    }

    @Override
    public <T extends RuleResult> T getResult(long ruleId) {
        return null;
    }

    @Override
    public long findId(RuleRequest request) {
        return -1;
    }

    @Override
    public Collection<String> getKnownRequests() {
        return arbiterFactory.getKnownRequests().stream()
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Class<? extends RuleResult> getResultClass(RuleRequest request) {
        return arbiterFactory.getResultClass(request.getClass());
    }

    @Override
    public Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass) {
        return arbiterFactory.getResultClass(requestClass);
    }

    @Override
    public void cancel(long ruleId) {
        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new RuleCancellationEvent(this, ruleId));
        }
    }

    @Override
    public Collection<RuleInfo> getRuleInfo() {
        return null;
    }

    @Override
    public Collection<RuleInfo> getRuleInfo(Class<? extends RuleRequest> requestClass) {
        return null;
    }
}
