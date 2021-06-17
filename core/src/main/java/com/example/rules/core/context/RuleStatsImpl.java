package com.example.rules.core.context;

import com.example.rules.api.FactStatistic;
import com.example.rules.spi.RuleStats;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleSession;

import java.util.Map;

public class RuleStatsImpl implements RuleStats {

    @Override
    public Map<String, Integer> getRuleStatistics() {
        return null;
    }

    @Override
    public Map<String, FactStatistic> getFactStatistics() {
        return null;
    }

    @Override
    public int getRuleCount() {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> int getRuleCount(Class<A> clazz) {
        return 0;
    }

    @Override
    public long getRuleDuration() {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> long getRuleDuration(Class<A> arbiterClass) {
        return 0;
    }

    @Override
    public <A extends Arbiter<?, ?>> void startRules(Class<A> arbiterClass) {

    }

    @Override
    public <A extends Arbiter<?, ?>> void finishRules(Class<A> arbiterClass, RuleSession session) {

    }

    @Override
    public int getFactCount() {
        return 0;
    }

    @Override
    public int getFactCount(Class<?> factClass) {
        return 0;
    }

    @Override
    public long getFactDuration() {
        return 0;
    }

    @Override
    public long getFactDuration(Class<?> factClass) {
        return 0;
    }

    @Override
    public void startFacts(Class<?> factClass) {

    }

    @Override
    public void finishFacts(Class<?> factClass, int count) {

    }
}
