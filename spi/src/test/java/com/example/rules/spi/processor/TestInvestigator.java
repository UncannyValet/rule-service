package com.example.rules.spi.processor;

import com.example.rules.spi.RuleContext;
import com.example.rules.spi.investigator.AbstractInvestigator;
import com.example.rules.spi.investigator.DependsUpon;

@DependsUpon(TestInvestigator.class)
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
