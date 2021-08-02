package com.example.rules.spi.utils;

import com.example.rules.api.RuleException;
import com.example.rules.spi.arbiter.AbstractArbiter;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.processor.TestArbiter;
import com.example.rules.spi.processor.TestRequest;
import com.example.rules.spi.processor.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClassUtilsTest {

    @Test
    public void testInstantiation() {
        assertTrue(ClassUtils.canInstantiate(TestArbiter.class));
        assertFalse(ClassUtils.canInstantiate(AbstractArbiter.class));
        assertFalse(ClassUtils.canInstantiate(Arbiter.class));

        assertNotNull(ClassUtils.instantiate(TestResult.class));
        assertThrows(RuleException.class, () -> ClassUtils.instantiate(Serializable.class));
    }

    @Test
    public void testParameterExtraction() {
        assertEquals(TestRequest.class, ClassUtils.getTypeArgument(TestArbiter.class, Arbiter.class, 0));
        assertEquals(TestResult.class, ClassUtils.getTypeArgument(TestArbiter.class, Arbiter.class, 1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> ClassUtils.getTypeArgument(TestArbiter.class, Arbiter.class, 2));

        assertNull(ClassUtils.getTypeArgument(AbstractArbiter.class, Arbiter.class, 0));
    }
}
