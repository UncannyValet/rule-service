package com.example.rules.core;

import com.example.rules.api.*;
import com.example.rules.core.arbiter.ArbiterFactory;
import com.example.rules.core.context.RuleContextFactory;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleCancellationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Future;

@Service
public class RuleServiceImpl implements RuleService {

    private final ArbiterFactory arbiterFactory;
    private final RuleContextFactory ruleContextFactory;

    private ApplicationEventPublisher applicationEventPublisher;
    private AsyncTaskExecutor arbiterExecutor;

    public RuleServiceImpl(ArbiterFactory arbiterFactory, RuleContextFactory ruleContextFactory) {
        this.arbiterFactory = arbiterFactory;
        this.ruleContextFactory = ruleContextFactory;
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
    public Future<RuleResult> schedule(RuleRequest request) {
        return arbiterExecutor.submit(() -> run(request));
    }

    @Override
    public <T extends RuleResult> T run(RuleRequest request) {
        RuleContext context = ruleContextFactory.newContext(request);
        Arbiter<RuleRequest, T> arbiter = arbiterFactory.getArbiter(context);
        return arbiter.processRules();
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
