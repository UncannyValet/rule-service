package com.example.rules.core.session;

import com.example.rules.spi.session.RuleSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.*;

public class CompoundSession implements RuleSession {

    private static final Logger LOG = LoggerFactory.getLogger(CompoundSession.class);

    private final Collection<? extends RuleSession> sessions;

    public CompoundSession(Collection<RuleSession> sessions) {
        this.sessions = sessions;
    }

    @Override
    public <F> void insert(F fact) {
        sessions.forEach(s -> s.insert(fact));
    }

    @Override
    public <T> Stream<T> getFacts(Class<T> factClass) {
        return sessions.stream().flatMap(session -> session.getFacts(factClass));
    }

    @Override
    public void setGlobal(String identifier, Object value) {
        sessions.forEach(s -> s.setGlobal(identifier, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getGlobal(String identifier) {
        return (T)sessions.stream()
                .map(session -> session.getGlobal(identifier))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    @Override
    public int runRules() {
        return sessions.stream()
                .mapToInt(RuleSession::runRules)
                .sum();
    }

    @Override
    public Stream<Object[]> query(String queryId, String[] objectNames, Object... arguments) {
        return sessions.stream().flatMap(s -> s.query(queryId, objectNames, arguments));
    }

    @Override
    public void halt() {
        sessions.forEach(RuleSession::halt);
    }

    @Override
    public int getRuleCount() {
        return sessions.stream().mapToInt(RuleSession::getRuleCount).sum();
    }

    @Override
    public long getFactCount() {
        return sessions.stream().mapToLong(RuleSession::getFactCount).sum();
    }

    @Override
    public void setLogger(Logger log) {
        sessions.forEach(session -> session.setLogger(log));
    }

    @Override
    public Map<String, Integer> getRuleHistogram() {
        return sessions.stream()
                .map(RuleSession::getRuleHistogram)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }

    @Override
    public void close() {
        for (RuleSession session : sessions) {
            try {
                session.close();
            } catch (Exception e) {
                LOG.error("Error closing rules session", e);
            }
        }
    }

    @Override
    public <E extends EventListener> void addEventListener(E listener) {
        sessions.forEach(session -> session.addEventListener(listener));
    }
}
