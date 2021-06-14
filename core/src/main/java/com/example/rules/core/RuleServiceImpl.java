package com.example.rules.core;

import com.example.rules.api.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Future;

@Service
public class RuleServiceImpl implements RuleService {
    @Override
    public Future<RuleResult> schedule(RuleRequest request) {
        return null;
    }

    @Override
    public <T extends RuleResult> T run(RuleRequest request) {
        return null;
    }

    @Override
    public <T extends RuleResult> T run(String id, RuleRequest request) {
        return null;
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
    public <T extends RuleResult> T getResult(String ruleId) {
        return null;
    }

    @Override
    public boolean cancel(String ruleId) {
        return false;
    }

    @Override
    public String findId(RuleRequest request) {
        return null;
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
