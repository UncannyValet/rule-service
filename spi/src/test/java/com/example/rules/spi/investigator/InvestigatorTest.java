package com.example.rules.spi.investigator;

import com.example.rules.spi.RuleContext;
import com.example.rules.spi.processor.TestInvestigator;
import com.example.rules.spi.session.RuleSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InvestigatorTest {

    @Mock
    private RuleContext context;

    @Mock
    private RuleSession session;

    private TestInvestigator investigator;

    @BeforeEach
    public void setup() {
        investigator = new TestInvestigator(context);
    }

    @Test
    public void testInvestigation() {
        investigator.gatherFacts(session);
    }

    @Test
    public void testDependencies() {
        assertTrue(investigator.dependsOn(Collections.singleton(investigator)));
        assertFalse(investigator.dependsOn(Collections.emptySet()));
    }

    @Test
    public void testContext() {
        assertSame(context, investigator.getContext());
    }
}
