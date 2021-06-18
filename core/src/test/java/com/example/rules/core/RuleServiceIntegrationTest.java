package com.example.rules.core;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.core.arbiter.ArbiterFactory;
import com.example.rules.core.context.RuleContextFactory;
import com.example.rules.core.processor.TestArbiter;
import com.example.rules.fact.TestRequest;
import com.example.rules.fact.TestResult;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

public class RuleServiceIntegrationTest {

    private ApplicationContext context;

    @BeforeEach
    public void setup() {
        context = new AnnotationConfigApplicationContext("com.example.rules");
    }

    @Test
    public void findResultClass() {
        Assertions.assertEquals(TestResult.class, context.getBean(ArbiterFactory.class).getResultClass(TestRequest.class));
    }

    @Test
    public void findArbiter() {
        RuleContextFactory factory = context.getBean(RuleContextFactory.class);
        RuleContext ruleContext = factory.newContext(new TestRequest(5));
        Arbiter<RuleRequest, ? extends RuleResult> arbiter = context.getBean(ArbiterFactory.class).getArbiter(ruleContext);
        assertNotNull(arbiter);
        assertTrue(TestArbiter.class.isInstance(arbiter));
    }
}
