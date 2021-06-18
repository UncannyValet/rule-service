package com.example.rules.core.session;

import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.core.drools.DroolsContainer;
import com.example.rules.core.drools.RuleUpdateWorker;
import com.example.rules.spi.session.RuleContainer;
import com.example.rules.spi.session.RuleSession;
import com.example.rules.spi.session.SessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.kiescanner.KieScannerEventListener;
import org.kie.api.event.kiescanner.KieScannerStatusChangeEvent;
import org.kie.api.event.kiescanner.KieScannerUpdateResultsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static com.example.rules.api.ErrorNumbers.DUPLICATE_CONTAINER;

@Component
public class SessionFactoryImpl implements SessionFactory {

    private static final Logger LOG = LogManager.getLogger(SessionFactoryImpl.class);

    private final KieServices kieServices;
    private final Map<RuleContainer, Set<String>> containers = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> containerUpdates = new ConcurrentHashMap<>();
    private final Map<Class<? extends RuleRequest>, Set<String>> registeredSessions = new ConcurrentHashMap<>();
    private final RuleContainer defaultContainer;

    private TaskScheduler scheduler;

    public SessionFactoryImpl() {
        kieServices = KieServices.get();
        defaultContainer = new DroolsContainer(kieServices.getKieClasspathContainer());
    }

    @Autowired
    @Qualifier("kieUpdateScheduler")
    public void setScheduler(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public synchronized void registerContainer(String groupId, String artifactId, String versionRange) {
        String key = groupId + ":" + artifactId;
        ReleaseId releaseId = kieServices.newReleaseId(groupId, artifactId, versionRange);
        if (containers.keySet().stream()
                .map(RuleContainer::getId)
                .anyMatch(key::equalsIgnoreCase)) {
            throw new RuleException(DUPLICATE_CONTAINER, key);
        }

        DroolsContainer container = new DroolsContainer(kieServices.newKieContainer(releaseId));
        registerContainer(container);
        scheduleUpdates(container);
    }

    @Override
    public synchronized void registerContainer(RuleContainer container) {
        containers.remove(container);
        containers.put(container, Collections.emptySet());
        String id = container.getId();
        LOG.info("Registered container '" + id + "'");
        registerContainerSessions(container);
    }

    @Override
    public synchronized void deregisterContainer(String id) {
        Future<?> future = containerUpdates.get(id);
        if (future != null) {
            future.cancel(false);
            containerUpdates.remove(id);
        }

        containers.keySet().stream()
                .filter(c -> id.equalsIgnoreCase(c.getId()))
                .findAny()
                .ifPresent(containers::remove);

        LOG.info("De-registered container '" + id + "'");
    }

    /**
     * Schedules a KieScanner to monitor Maven for new versions of this container.
     *
     * @param container the DroolsContainer to update
     */
    private void scheduleUpdates(DroolsContainer container) {
        String id = container.getId();
        try {
            int interval = 5;
            KieScanner scanner = kieServices.newKieScanner(container.getKieContainer());
            scanner.addListener(new ScannerListener(container));
            LOG.info("Scheduling rules container '" + id + "' to scan for updates every " + interval + "s");
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(new RuleUpdateWorker(scanner), Duration.ofMinutes(interval));
            Future<?> oldFuture = containerUpdates.put(id, future);
            if (oldFuture != null) {
                // In case the old future is still in there
                oldFuture.cancel(false);
            }
        } catch (RuntimeException e) {
            LOG.error("Failed to schedule scanner for container " + id, e);
        }
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
            LOG.info("Rules container '" + container.getId() + "' available, provides sessions " + sessions);
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
            LOG.info("Registered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
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
                LOG.info("De-registered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
                if (sessions.isEmpty()) {
                    registeredSessions.remove(requestClass);
                    LOG.info("Request type '" + requestClass.getSimpleName() + "' has no configured sessions remaining");
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

    public RuleSession getSession(Collection<String> sessionIds) {
        // Search for the session IDs in the defined containers
        List<RuleSession> sessions = sessionIds.stream()
                .map(id -> {
                    RuleSession session = getSession(id);
                    if (session == null) {
                        LOG.error("Unknown rules session '" + id + "'");
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

    private class ScannerListener implements KieScannerEventListener {

        private final DroolsContainer container;

        ScannerListener(DroolsContainer container) {
            this.container = container;
        }

        @Override
        public void onKieScannerStatusChangeEvent(KieScannerStatusChangeEvent event) {
            // Scans are performed manually, scanner state is not used
        }

        @Override
        public void onKieScannerUpdateResultsEvent(KieScannerUpdateResultsEvent event) {
            registerContainerSessions(container);
        }
    }
}
