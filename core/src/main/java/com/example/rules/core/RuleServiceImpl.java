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

@Service
public class RuleServiceImpl implements RuleService {

    private final ArbiterFactory arbiterFactory;
    private final RuleContextFactory ruleContextFactory;

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
        RuleLog logEntry = createLog(request);
        return arbiterExecutor.submit(() -> run(request, logEntry.getId()));
    }

    @Override
    public long schedule(RuleRequest request) {
        RuleLog logEntry = createLog(request);
        arbiterExecutor.submit(() -> run(request, logEntry.getId()));
        return logEntry.getId();
    }

    @Override
    public <T extends RuleResult> T run(RuleRequest request) {
        RuleLog logEntry = createLog(request);
        return run(request, logEntry.getId());
    }

    private <T extends RuleResult> T run(RuleRequest request, long runId) {
        logRepository.findById(runId).ifPresent(l -> {
            l.setUpdateTime(LocalDateTime.now());
            l.setState("RUNNING");
            logRepository.save(l);
        });

        try {
            RuleContext context = ruleContextFactory.newContext(request);
            Arbiter<RuleRequest, T> arbiter = arbiterFactory.getArbiter(context);
            T result = arbiter.processRules();

            logRepository.findById(runId).ifPresent(l -> {
                l.setUpdateTime(LocalDateTime.now());
                l.setState("SUCCESS");
                l.setResultClass(result.getClass().getName());
                l.setResultDescription(result.getDescription());
                logRepository.save(l);
            });

            return result;
        } catch (Exception e) {
            logRepository.findById(runId).ifPresent(l -> {
                l.setUpdateTime(LocalDateTime.now());
                l.setState("FAILURE");
                logRepository.save(l);
            });
            return null;
        }
    }

    private RuleLog createLog(RuleRequest request) {
        RuleLog logEntry = new RuleLog();
        logEntry.setState("CREATED");
        logEntry.setRequestClass(request.getClass().getName());
        logEntry.setRequestHash(request.hashCode());
        logEntry.setRequestData(RuleSerializer.serialize(request));
        logEntry.setRequestDescription(request.getDescription());
        return logRepository.save(logEntry);
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
        return null;
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
