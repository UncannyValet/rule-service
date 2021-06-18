package com.example.rules.core.drools;

import com.example.rules.spi.session.RuleSession;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Logger;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.*;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.QueryResults;

import java.util.*;
import java.util.stream.Stream;

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
    public <T> Stream<T> getFactStream(Class<T> factClass) {
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
//        RuleNameFilter filter = new RuleNameFilter();
//        int count = filter.isEmpty() ? session.fireAllRules() : session.fireAllRules(filter);
        int count = session.fireAllRules();
        if (log != null) {
            ruleCounts.forEach((name, value) -> log.info("- Rule '" + name + "' asserted " + value.intValue() + " time(s)"));
        }
        return count;
    }

    public Agenda getAgenda() {
        return session.getAgenda();
    }

    public QueryResults getQueryResults(String query, Object... arguments) {
        return session.getQueryResults(query, arguments);
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

        Map<String, Integer> retval = new HashMap<>(ruleCounts.size());
        ruleCounts.forEach((id, cnt) -> retval.put(id, cnt.intValue()));
        return retval;
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

//    private static class RuleNameFilter implements AgendaFilter {
//
//        private final Set<String> enabledRules;
//        private final Set<String> disabledRules;
//
//        RuleNameFilter() {
//            Configuration config = ConfigurationFactory.getInstance().getConfiguration().subset("rules.control");
//            String[] s = config.getStringArray("enabled.rules");
//            if (s.length > 0) {
//                enabledRules = Arrays.stream(s)
//                        .filter(StringUtils::isNotEmpty)
//                        .map(String::trim)
//                        .map(String::toLowerCase)
//                        .collect(Collectors.toCollection(HashSet::new));
//            } else {
//                enabledRules = Collections.emptySet();
//            }
//            s = config.getStringArray("disabled.rules");
//            if (s.length > 0) {
//                disabledRules = Arrays.stream(s)
//                        .filter(StringUtils::isNotEmpty)
//                        .map(String::trim)
//                        .map(String::toLowerCase)
//                        .collect(Collectors.toCollection(HashSet::new));
//            } else {
//                disabledRules = Collections.emptySet();
//            }
//        }
//
//        boolean isEmpty() {
//            return enabledRules.isEmpty() && disabledRules.isEmpty();
//        }
//
//        @Override
//        public boolean accept(Match match) {
//            Rule rule = match.getRule();
//            String ruleName = rule.getName().toLowerCase();
//            Map<String, Object> metadata = rule.getMetaData();
//            String ruleId = metadata.containsKey("id") ? ((String)metadata.get("id")).toLowerCase() : "";
//            if (!enabledRules.isEmpty()) {
//                return enabledRules.contains(ruleName) || enabledRules.contains(ruleId);
//            }
//            return !(disabledRules.contains(ruleName) || disabledRules.contains(ruleId));
//        }
//    }
}
