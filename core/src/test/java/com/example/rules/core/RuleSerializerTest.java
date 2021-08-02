package com.example.rules.core;

import com.example.rules.core.repository.RuleSerializer;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleSerializerTest {

    @Test
    public void serialize() {
        TestClass object = new TestClass();
        object.setId("Test");
        object.setValue(5);

        String serializedString = RuleSerializer.serializeAsString(object);
        assertTrue(serializedString.contains("<id>Test</id>"));
        assertTrue(serializedString.contains("<value>5</value>"));

        byte[] serializedBytes = RuleSerializer.serialize(object);
        assertTrue(serializedBytes.length > 0);

        assertEquals(serializedString, RuleSerializer.deserializeAsString(serializedBytes));
        assertEquals(object, RuleSerializer.deserialize(serializedString));
        assertEquals(object, RuleSerializer.deserialize(serializedBytes));

        // Verify no exception thrown
        assertEquals("", RuleSerializer.deserializeAsString(new byte[]{1, 2, 3}));
    }

    public static class TestClass {

        private String id;
        private int value;

        public void setId(String id) {
            this.id = id;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass)o;
            return value == testClass.value && Objects.equals(id, testClass.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }
}
