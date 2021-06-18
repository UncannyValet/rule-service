package com.example.rules.core.processor;

import com.example.rules.fact.TestRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.investigator.AbstractInvestigator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class TestInvestigator extends AbstractInvestigator<TestRequest, String> {

    public TestInvestigator(RuleContext context) {
        super(context);
    }

    @Override
    protected void doGather() {
        insert("Fact");
    }

    @Override
    protected void beforeGather() {
        super.beforeGather();
    }

    @Override
    protected void afterGather() {
        super.afterGather();
    }
}
