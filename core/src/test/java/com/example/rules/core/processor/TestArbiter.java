package com.example.rules.core.processor;

import com.example.rules.fact.TestRequest;
import com.example.rules.fact.TestResult;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.AbstractArbiter;
import com.example.rules.spi.arbiter.RuleSet;
import com.example.rules.spi.session.RuleSession;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@RuleSet("test_a")
@Component
@Scope(SCOPE_PROTOTYPE)
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
        session.setGlobal("res", getResult());
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
