package com.example.rules.spi.session;

import com.example.rules.api.RuleInfo;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Functionality for an object that provides RulesSessions
 */
public interface RuleContainer {

    /**
     * Returns the ID of the container.
     */
    String getId();

    /**
     * Returns a Set of sessions provided by this container.
     */
    Set<String> getProvidedSessions();

    /**
     * Creates a new RulesSession for the given ID.
     *
     * @param sessionId the ID of the RulesSession
     * @return a new RulesSession
     */
    RuleSession newSession(String sessionId);

    /**
     * Returns a Stream of the RuleInfo stored in this container.
     */
    Stream<RuleInfo> getRuleInfo();
}
