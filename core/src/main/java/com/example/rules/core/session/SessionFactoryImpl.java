package com.example.rules.core.session;

import com.example.rules.api.RuleInfo;
import com.example.rules.api.RuleRequest;
import com.example.rules.core.drools.DroolsContainer;
import com.example.rules.spi.session.*;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class SessionFactoryImpl implements SessionFactory {

    private final Map<RuleContainer, Set<String>> containers = new ConcurrentHashMap<>();
    private final Map<Class<? extends RuleRequest>, Set<String>> registeredSessions = new ConcurrentHashMap<>();
    private final RuleContainer defaultContainer;

    public SessionFactoryImpl() {
        defaultContainer = new DroolsContainer(KieServices.get().getKieClasspathContainer());
    }

    @Override
    public synchronized void registerContainer(RuleContainer container) {
        containers.remove(container);
        containers.put(container, Collections.emptySet());
        String id = container.getId();
        log.info("Registered container '" + id + "'");
        registerContainerSessions(container);
    }

    @Override
    public synchronized void deregisterContainer(String id) {
        containers.keySet().stream()
                .filter(c -> id.equalsIgnoreCase(c.getId()))
                .findAny()
                .ifPresent(containers::remove);

        log.info("De-registered container '" + id + "'");
    }

    /**
     * Registers sessions contained within a RulesContainer.
     *
     * @param container the RulesContainer
     */
    private void registerContainerSessions(RuleContainer container) {
        Set<String> sessions = container.getProvidedSessions();

        if (containers.containsKey(container)) {
            // Protect against cases where a container has been de-registered but the update worker is still running
            containers.put(container, sessions);
            log.info("Rules container '" + container.getId() + "' available, provides sessions " + sessions);
        }
    }

    /**
     * Registers a rules session to be used for a given request class
     * <p>This is typically used by modules that provide additional rules to run on an existing request class</p>
     *
     * @param requestClass the RulesRequest class
     * @param sessionId    the ID of the session to add to the request processing flow
     */
    public void registerSession(Class<? extends RuleRequest> requestClass, String sessionId) {
        synchronized (registeredSessions) {
            registeredSessions.computeIfAbsent(requestClass, k -> new HashSet<>()).add(sessionId);
            log.info("Registered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
        }
    }

    /**
     * De-registers a rules session from a given request class
     *
     * @param requestClass the RulesRequest class
     * @param sessionId    the ID of the session to remove from the request processing flow
     */
    public void deregisterSession(Class<? extends RuleRequest> requestClass, String sessionId) {
        synchronized (registeredSessions) {
            Collection<String> sessions = registeredSessions.get(requestClass);
            if (sessions != null) {
                sessions.remove(sessionId);
                log.info("De-registered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
                if (sessions.isEmpty()) {
                    registeredSessions.remove(requestClass);
                    log.info("Request type '" + requestClass.getSimpleName() + "' has no configured sessions remaining");
                }
            }
        }
    }

    /**
     * Gets a collection of registered session IDs to use for a given request class
     *
     * @param requestClass the RulesRequest class
     * @return a non-null Collection of registered session IDs for the given class
     */
    public Collection<String> getRegisteredSessions(Class<? extends RuleRequest> requestClass) {
        synchronized (registeredSessions) {
            Collection<String> sessions = registeredSessions.get(requestClass);
            if (sessions == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(sessions);
            }
        }
    }

    @Override
    public <R extends RuleRequest> RuleSession getSession(R request, String... ruleSets) {
        Set<String> sessionIds = new HashSet<>(Arrays.asList(ruleSets));
        sessionIds.addAll(getRegisteredSessions(request.getClass()));
        return getSession(sessionIds);
    }

    @Override
    public Stream<RuleInfo> getRuleInfo() {
        return Stream.concat(Stream.of(defaultContainer), containers.keySet().stream())
                .flatMap(RuleContainer::getRuleInfo);
    }

    @Override
    public Stream<RuleInfo> getRuleInfo(RuleRequest request) {
        return null;
    }

    public RuleSession getSession(Collection<String> sessionIds) {
        // Search for the session IDs in the defined containers
        List<RuleSession> sessions = sessionIds.stream()
                .map(id -> {
                    RuleSession session = getSession(id);
                    if (session == null) {
                        log.error("Unknown rules session '" + id + "'");
                    }
                    return session;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new CompoundSession(sessions);
    }

    /**
     * Finds a session matching the given ID in the known containers
     *
     * @param sessionId the session ID
     * @return a new RulesSession
     */
    RuleSession getSession(String sessionId) {
        return getContainer(sessionId).newSession(sessionId);
    }

    /**
     * Finds a container that provides the given session
     *
     * @param sessionId the session ID
     * @return a registered container, or the classpath container if no other container provides the session
     */
    private RuleContainer getContainer(String sessionId) {
        return containers.entrySet().stream()
                .filter(e -> e.getValue().contains(sessionId))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(defaultContainer);
    }
}
