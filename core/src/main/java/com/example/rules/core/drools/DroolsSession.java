package com.example.rules.core.drools;

import com.example.rules.spi.session.RuleSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.*;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.*;

/**
 * Wrapper class for Drools sessions
 */
public class DroolsSession implements RuleSession {

    private final KieSession session;
    private final Map<String, MutableInt> ruleCounts = new HashMap<>();

    private Logger log;

    public DroolsSession(KieSession session) {
        this.session = session;
    }

    @Override
    public <F> void insert(F fact) {
        session.insert(fact);
    }

    @Override
    public <T> Stream<T> getFacts(Class<T> factClass) {
        return session.getObjects().stream()
                .filter(factClass::isInstance)
                .map(factClass::cast);
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public void setGlobal(String identifier, Object value) {
        try {
            session.setGlobal(identifier, value);
        } catch (RuntimeException e) {
            if (log != null) {
                log.debug("Cannot set global '" + identifier + "': " + e.getMessage());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getGlobal(String identifier) {
        return (T)session.getGlobal(identifier);
    }

    @Override
    public int runRules() {
        RuleNameFilter filter = new RuleNameFilter();
        int count = filter.isEmpty() ? session.fireAllRules() : session.fireAllRules(filter);
        if (log != null) {
            ruleCounts.forEach((name, value) -> log.info("- Rule '" + name + "' asserted " + value.intValue() + " time(s)"));
        }
        return count;
    }

    @Override
    public Stream<Object[]> query(String queryId, String[] objectNames, Object... arguments) {
        QueryResults results = session.getQueryResults(queryId, arguments);
        if (results.size() > 0) {
            return StreamSupport.stream(results.spliterator(), false)
                    .map(r -> {
                        Object[] result = {objectNames.length};
                        for (int i = 0; i < objectNames.length; ++i) {
                            result[i] = r.get(objectNames[i]);
                        }
                        return result;
                    });
        }
        return Stream.empty();
    }

    @Override
    public void halt() {
        session.halt();
    }

    @Override
    public int getRuleCount() {
        return session.getKieBase().getKiePackages().stream()
                .mapToInt(pkg -> pkg.getRules().size())
                .sum();
    }

    @Override
    public Map<String, Integer> getRuleHistogram() {
        if (ruleCounts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> countMap = new HashMap<>(ruleCounts.size());
        ruleCounts.forEach((id, cnt) -> countMap.put(id, cnt.intValue()));
        return countMap;
    }

    @Override
    public void close() {
        session.dispose();
    }

    @Override
    public void setLogger(Logger log) {
        this.log = log;
        session.addEventListener(new AgendaEventLogger());
    }

    @Override
    public <E extends EventListener> void addEventListener(E listener) {
        if (listener instanceof RuleRuntimeEventListener) {
            session.addEventListener((RuleRuntimeEventListener)listener);
        } else if (listener instanceof AgendaEventListener) {
            session.addEventListener((AgendaEventListener)listener);
        } else if (listener instanceof ProcessEventListener) {
            session.addEventListener((ProcessEventListener)listener);
        } else {
            throw new IllegalArgumentException("Incompatible Drools session listener " + listener.getClass().getSimpleName());
        }
    }

    private class AgendaEventLogger extends DefaultAgendaEventListener {
        @Override
        public void afterMatchFired(AfterMatchFiredEvent event) {
            ruleCounts.computeIfAbsent(event.getMatch().getRule().getName(), k -> new MutableInt()).increment();
        }
    }

    private static class RuleNameFilter implements AgendaFilter {

        private final Set<String> enabledRules;
        private final Set<String> disabledRules;

        RuleNameFilter() {
            String s = System.getProperty("drools.rules.enabled", "");
            if (StringUtils.isNotEmpty(s)) {
                enabledRules = Arrays.stream(s.split(","))
                        .filter(StringUtils::isNotEmpty)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toCollection(HashSet::new));
            } else {
                enabledRules = Collections.emptySet();
            }
            s = System.getProperty("drools.rules.disabled", "");
            if (StringUtils.isNotEmpty(s)) {
                disabledRules = Arrays.stream(s.split(","))
                        .filter(StringUtils::isNotEmpty)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toCollection(HashSet::new));
            } else {
                disabledRules = Collections.emptySet();
            }
        }

        boolean isEmpty() {
            return enabledRules.isEmpty() && disabledRules.isEmpty();
        }

        @Override
        public boolean accept(Match match) {
            Rule rule = match.getRule();
            String ruleName = rule.getName().toLowerCase();
            Map<String, Object> metadata = rule.getMetaData();
            String ruleId = metadata.containsKey("id") ? ((String)metadata.get("id")).toLowerCase() : "";
            if (!enabledRules.isEmpty()) {
                return enabledRules.contains(ruleName) || enabledRules.contains(ruleId);
            }
            return !(disabledRules.contains(ruleName) || disabledRules.contains(ruleId));
        }
    }
}
