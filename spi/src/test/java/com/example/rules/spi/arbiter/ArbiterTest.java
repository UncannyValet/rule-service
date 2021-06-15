package com.example.rules.spi.arbiter;

import com.example.rules.spi.RuleContext;
import com.example.rules.spi.processor.TestArbiter;
import com.example.rules.spi.processor.TestResult;
import com.example.rules.spi.session.RuleSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArbiterTest {

    @Mock
    private RuleContext context;

    @Mock
    private RuleSession session;

    private TestArbiter arbiter;

    @BeforeEach
    public void setup() {
        arbiter = new TestArbiter(context);
    }

    @Test
    public void testArbitration() {
        when(context.newSession(any())).thenReturn(session);
        when(session.getRuleCount()).thenReturn(1);
        TestResult result = arbiter.processRules();
        assertNotNull(result);
        assertSame(result, arbiter.getResult());
    }

    @Test
    public void testContext() {
        assertSame(context, arbiter.getContext());
    }
}
