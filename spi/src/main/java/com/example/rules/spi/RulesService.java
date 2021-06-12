package com.example.rules.spi;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.annotation.Named;
import com.daxtechnologies.database.annotation.Table;
import com.daxtechnologies.database.orm.activerecord.ActiveRecord;
import com.daxtechnologies.oam.ILogger;
import com.daxtechnologies.oam.TheLogger;
import com.daxtechnologies.record.filter.Expression;
import com.daxtechnologies.services.AbstractService;
import com.daxtechnologies.services.Services;
import com.daxtechnologies.services.trace.Trace;
import com.daxtechnologies.util.ArgumentUtilities;
import com.daxtechnologies.util.StringUtils;
import com.daxtechnologies.util.collection.MapFactory;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;
import com.spirent.cem.rules.spi.arbiter.Arbiter;
import com.spirent.cem.rules.spi.compiler.Rule;
import com.spirent.cem.rules.spi.compiler.RulePackage;
import com.spirent.cem.rules.spi.dimension.DimensionManager;
import com.spirent.cem.rules.spi.investigator.Investigator;
import com.spirent.cem.rules.spi.session.RulesContainer;
import com.spirent.cem.rules.spi.session.RulesSession;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

@Named("Rules Service")
public abstract class RulesService extends AbstractService implements com.spirent.cem.rules.api.RulesService {

    protected static final ILogger LOG = TheLogger.getInstance(RulesService.class);

    private final Map<Class<? extends RulesRequest>, BiConsumer<? extends RulesRequest, RulesContext>> successCallbacks = MapFactory.concurrent();
    private com.spirent.cem.rules.spi.ProcessorRegistry registry;
    private DimensionManager dimensionManager;
    private final Set<String> requests = new HashSet<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends ActiveRecord>, Object> registeredDimensions = MapFactory.concurrent();
    private final Map<Class<? extends RulesRequest>, Set<String>> registeredSessions = new HashMap<>();

    public static RulesService getInstance() {
        return Services.lookupService(RulesService.class);
    }

    @Override
    protected void doInitialize() {
        super.doInitialize();

        registry = new com.spirent.cem.rules.spi.ProcessorRegistry();
        registry.initialize();

        dimensionManager = new DimensionManager();

        AnnotationUtilities.getSubClasses(RulesRequest.class).forEach(c -> requests.add(c.getName()));
    }

    /**
     * Registers a rules container for resolution through Maven
     *
     * @param groupId      the Group ID
     * @param artifactId   the Artifact ID
     * @param versionRange the version range: [x] for fixed version, [x,y) for range, [x,) for open ended
     */
    public abstract void registerContainer(String groupId, String artifactId, String versionRange);

    /**
     * Register a RulesContainer directly with the service
     *
     * @param container the container to register
     */
    public abstract void registerContainer(RulesContainer container);

    /**
     * Removes a rules container from resolution through Maven
     *
     * @param id the Container ID
     */
    public abstract void deregisterContainer(String id);

    /**
     * Gets a new RulesSession for the provided IDs
     *
     * @param sessionIds a Collection of session IDs
     * @return a new RulesSession for the given IDs
     */
    public abstract RulesSession getSession(Collection<String> sessionIds);

    @Override
    public Collection<String> getKnownRequests() {
        return requests;
    }

    @Override
    public Class<? extends RulesResult> getResultClass(RulesRequest request) {
        return registry.getResultClass(request);
    }

    @Override
    public Class<? extends RulesResult> getResultClass(Class<? extends RulesRequest> requestClass) {
        return registry.getResultClass(requestClass);
    }

    /**
     * Schedules an Investigator for asynchronous execution
     *
     * @param investigator the Investigator
     * @return a Future that will return the Investigator when get() is called
     */
    public abstract <R extends RulesRequest> Future<Investigator<R, ?>> scheduleInvestigation(Investigator<R, ?> investigator);

    /**
     * Registers an ActiveRecord type for use as a dimension
     *
     * @param recordType the Class of the ActiveRecord
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public final <R extends ActiveRecord<R>> void registerDimension(Class<R> recordType) {
        registerDimension(recordType, null);
    }

    /**
     * Registers an ActiveRecord type for use as a dimension
     *
     * @param recordType the Class of the ActiveRecord
     * @param filter     an Expression used to filter entries during warm up
     */
    @SuppressWarnings("WeakerAccess")
    public final <R extends ActiveRecord<R>> void registerDimension(Class<R> recordType, Expression filter) {
        dimensionManager.registerDimension(recordType, filter);
        registeredDimensions.put(recordType, true);
    }

    /**
     * Registers a rules session to be used for a given request class
     * <p>This is typically used by modules that provide additional rules to run on an existing request class</p>
     *
     * @param requestClass the RulesRequest class
     * @param sessionId    the ID of the session to add to the request processing flow
     */
    public void registerSession(Class<? extends RulesRequest> requestClass, String sessionId) {
        synchronized (registeredSessions) {
            registeredSessions.computeIfAbsent(requestClass, k -> new HashSet<>()).add(sessionId);
            LOG.info("Registered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
        }
    }

    /**
     * Deregisters a rules session from a given request class
     *
     * @param requestClass the RulesRequest class
     * @param sessionId    the ID of the session to remove from the request processing flow
     */
    public void deregisterSession(Class<? extends RulesRequest> requestClass, String sessionId) {
        synchronized (registeredSessions) {
            Collection<String> sessions = registeredSessions.get(requestClass);
            if (sessions != null) {
                sessions.remove(sessionId);
                LOG.info("Deregistered session '" + sessionId + "' for request type '" + requestClass.getSimpleName() + "'");
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
    public Collection<String> getRegisteredSessions(Class<? extends RulesRequest> requestClass) {
        synchronized (registeredSessions) {
            Collection<String> sessions = registeredSessions.get(requestClass);
            if (sessions == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(sessions);
            }
        }
    }

    /**
     * Resolves an ActiveRecord dimension entry
     *
     * @param recordType the ActiveRecord type
     * @param key        the record key
     * @param <R>        the ActiveRecord type
     * @return an ActiveRecord for the key, or {@code null} if none is found
     */
    public <R extends ActiveRecord<R>> R resolveDimension(Class<R> recordType, Object key) {
        return dimensionManager.resolveDimension(recordType, key);
    }

    @Override
    protected void doStart() {
        super.doStart();

        // Start warm-up and maintenance of dimensions
        dimensionManager.warmUp();
    }

    /**
     * Processes table change notifications for tables backing known dimensions and refreshes them with a new warm up
     *
     * @param tableName the table name
     */
    @SuppressWarnings("unchecked")
    public void tableChange(String tableName) {
        ArgumentUtilities.validateIfNotEmpty(tableName);
        registeredDimensions.keySet().stream()
                .filter(recordType -> {
                    Table table = recordType.getAnnotation(Table.class);
                    return table != null && StringUtils.isNotEmpty(table.name()) && table.name().equalsIgnoreCase(tableName);
                })
                .peek(recordType -> LOG.info("Received table change notification for " + recordType.getSimpleName()))
                .forEach(dimensionManager::warmUp);
    }

    /**
     * Registers a callback class to be executed when a request of type T is successfully completed
     *
     * @param requestClass the type of RulesRequest to handle
     * @param callback     a BiConsumer class that takes a request and its associated result
     * @param <T>          the type of RulesRequest to handle
     */
    public <T extends RulesRequest> void onRequestSuccess(Class<T> requestClass, BiConsumer<? extends RulesRequest, RulesContext> callback) {
        successCallbacks.put(requestClass, callback);
    }

    /**
     * Executes callbacks registered on success of a RulesRequest
     *
     * @param request the RulesRequest
     * @param context the rules run context
     */
    public <T extends RulesRequest> void handleSuccess(T request, RulesContext context) {
        Class<? extends RulesRequest> requestClass = request.getClass();
        BiConsumer<? extends RulesRequest, RulesContext> callback = successCallbacks.get(requestClass);
        if (callback != null) {
            @SuppressWarnings("unchecked")
            BiConsumer<T, RulesContext> c = (BiConsumer<T, RulesContext>)callback;
            Trace.doWithTask(request.getName() + " handler", v -> c.accept(request, context));
        }
    }

    /**
     * Gets a new Investigator to gather facts for a given request
     *
     * @param request   the RulesRequest
     * @param factClass the class of Fact to gather
     * @return a new Investigator matching the request, throwing an exception if none is found
     */
    public <R extends RulesRequest, F> Investigator<R, F> getInvestigator(R request, Class<F> factClass) {
        return registry.getInvestigator(request, factClass);
    }

    /**
     * Gets a Collection of Investigators to gather facts for a given request
     *
     * @param request the RulesRequest
     * @return a Collection of Investigators matching the request
     */
    public <R extends RulesRequest> Collection<Investigator<R, ?>> getInvestigators(R request) {
        return registry.getInvestigators(request);
    }

    /**
     * Gets the Arbiter class that will handle a given RulesRequest class
     *
     * @param requestClass the RulesRequest class
     * @return a non-null Arbiter class
     */
    @SuppressWarnings("WeakerAccess")
    public <R extends RulesRequest> Class<? extends Arbiter<R, ?>> getArbiterClass(Class<R> requestClass) {
        return registry.getArbiterClass(requestClass);
    }

    /**
     * Gets a new Arbiter to process a given request
     *
     * @param request the RulesRequest to process
     * @return a new Arbiter matching the request, throwing an exception if none is found
     */
    public <R extends RulesRequest> Arbiter<R, ?> getArbiter(R request) {
        return registry.getArbiter(request);
    }

    /**
     * Gets a new RulePackage Builder with the given ID and session
     *
     * @param id        the package ID
     * @param sessionId the sessionId
     * @return a new RulePackage Builder
     */
    public abstract RulePackage.Builder newRulePackage(String id, String sessionId);

    /**
     * Gets a new Rule Builder with the given ID
     *
     * @param id the rule ID
     * @return a new Rule Builder
     */
    public abstract Rule.Builder newRule(String id);
}
