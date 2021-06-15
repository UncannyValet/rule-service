package com.example.rules.spi.processor;

import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.AbstractArbiter;
import com.example.rules.spi.arbiter.RuleSet;
import com.example.rules.spi.session.RuleSession;

@RuleSet("test")
public class TestArbiter extends AbstractArbiter<TestRequest, TestResult> {

    public TestArbiter(RuleContext context) {
        super(context);
    }

    @Override
    protected void beforeSession() {
        super.beforeSession();
    }

    @Override
    protected void beforeFacts(RuleSession session) {
        super.beforeFacts(session);
    }

    @Override
    protected void beforeRules(RuleSession session) {
        super.beforeRules(session);
    }

    @Override
    protected void afterRules(RuleSession session) {
        super.afterRules(session);
    }

    @Override
    protected void afterSession() {
        super.afterSession();
    }
}
