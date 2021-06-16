package com.example.rules.core;

import com.example.rules.api.*;
import com.example.rules.core.arbiter.ArbiterFactory;
import com.example.rules.core.context.RuleContextFactory;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Future;

@Service
public class RuleServiceImpl implements RuleService {

    private final ArbiterFactory arbiterFactory;
    private final RuleContextFactory ruleContextFactory;

    public RuleServiceImpl(ArbiterFactory arbiterFactory, RuleContextFactory ruleContextFactory) {
        this.arbiterFactory = arbiterFactory;
        this.ruleContextFactory = ruleContextFactory;
    }

    @Override
    public Future<RuleResult> schedule(RuleRequest request) {
        return null;
    }

    @Override
    public <T extends RuleResult> T run(RuleRequest request) {
        RuleContext context = ruleContextFactory.newContext(request);
        Arbiter<RuleRequest, ? extends RuleResult> arbiter = arbiterFactory.getArbiter(context);
        return (T)arbiter.processRules();
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
        return null;
    }

    @Override
    public Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass) {
        return null;
    }

    @Override
    public boolean cancel(long ruleId) {
        return false;
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
