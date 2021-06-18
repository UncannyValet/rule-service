package com.example.rules.fact;

import com.example.rules.api.RuleRequest;

public class TestRequest implements RuleRequest {

    private final int threshold;

    public TestRequest(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestRequest that = (TestRequest)o;

        return threshold == that.threshold;
    }

    @Override
    public int hashCode() {
        return threshold;
    }
}
