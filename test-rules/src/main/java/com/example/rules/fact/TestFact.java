package com.example.rules.fact;

public class TestFact {

    private int value;

    public TestFact(int value) {
        this.value = value;
    }

    public TestFact() {
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TestFact{" +
                "value=" + value +
                '}';
    }
}
