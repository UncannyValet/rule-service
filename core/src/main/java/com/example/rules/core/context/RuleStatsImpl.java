package com.example.rules.core.context;

import com.example.rules.api.FactStatistic;
import com.example.rules.spi.RuleStats;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.session.RuleSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RuleStatsImpl implements RuleStats {

    private final Map<Class<? extends Arbiter<?, ?>>, Integer> ruleCounts = new HashMap<>();
    private final TimingTracker ruleTimings = new TimingTracker();
    private final Map<String, Integer> ruleHistogram = new HashMap<>();

    private final Map<Class<?>, Integer> factCounts = new HashMap<>();
    private final TimingTracker factTimings = new TimingTracker();

    @Override
    public Map<String, Integer> getRuleHistogram() {
        return ruleHistogram;
    }

    @Override
    public Map<String, FactStatistic> getFactStatistics() {
        if (factCounts.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, FactStatistic> stats = new HashMap<>();
        factCounts.forEach((clazz, count) -> stats.put(clazz.getSimpleName(), new FactStatistic(count, factTimings.getElapsed(clazz.getName()))));
        return stats;
    }

    @Override
    public int getRuleCount() {
        return ruleCounts.values().stream().mapToInt(value -> value).sum();
    }

    @Override
    public <A extends Arbiter<?, ?>> int getRuleCount(Class<A> clazz) {
        return ruleCounts.getOrDefault(clazz, 0);
    }

    @Override
    public long getRuleDuration() {
        return ruleTimings.getTotal();
    }

    @Override
    public <A extends Arbiter<?, ?>> long getRuleDuration(Class<A> arbiterClass) {
        return ruleTimings.getElapsed(arbiterClass.getName());
    }

    @Override
    public <A extends Arbiter<?, ?>> void startRules(Class<A> arbiterClass) {
        ruleTimings.restart(arbiterClass.getName());
    }

    @Override
    public <A extends Arbiter<?, ?>> void finishRules(Class<A> arbiterClass, RuleSession session) {
        String className = arbiterClass.getName();
        ruleTimings.end(className);
        Map<String, Integer> histogram = session.getRuleHistogram();
        int total = histogram.values().stream().mapToInt(Integer::intValue).sum();
        this.ruleCounts.merge(arbiterClass, total, (v1, v2) -> v1 + total);

        histogram.forEach((id, cnt) -> this.ruleHistogram.merge(id, cnt, Integer::sum));
    }

    @Override
    public int getFactCount() {
        return factCounts.values().stream().mapToInt(value -> value).sum();
    }

    @Override
    public int getFactCount(Class<?> factClass) {
        return factCounts.getOrDefault(factClass, 0);
    }

    @Override
    public long getFactDuration() {
        return factTimings.getActive();
    }

    @Override
    public long getFactDuration(Class<?> factClass) {
        return factTimings.getElapsed(factClass.getName());
    }

    @Override
    public void startFacts(Class<?> factClass) {
        factTimings.restart(factClass.getName());
    }

    @Override
    public void finishFacts(Class<?> factClass, int count) {
        String className = factClass.getName();
        factTimings.end(className);
        factCounts.merge(factClass, count, Integer::sum);
    }
}
