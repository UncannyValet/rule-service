package com.example.rules.spi.investigator;

//import com.daxtechnologies.exception.ExceptionUtilities;
//import com.daxtechnologies.oam.ILogger;
//import com.daxtechnologies.oam.TheLogger;
//import com.daxtechnologies.services.Provider;
//import com.daxtechnologies.services.trace.Trace;
//import com.daxtechnologies.util.*;
import com.example.rules.api.RuleRequest;
import com.example.rules.spi.Context;
//import com.example.rules.spi.processor.AbstractRulesProcessor;
import com.example.rules.spi.session.RuleSession;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import jodd.mutable.MutableLong;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract implementation of the Investigator interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the Fact class associated with this processor
 */
//@Provider
public abstract class AbstractInvestigator<R extends RuleRequest, F> /*extends AbstractRulesProcessor<R>*/ implements Investigator<R, F> {

    @SuppressWarnings("squid:S00116")
    protected final Logger LOG = LogManager.getLogger(getClass());

    private RuleSession session;
    private int factCount;
    private Class<F> factClass;
//    private boolean enabled;
//    private Trace parentTrace;
    private boolean isDebug;
    private final Map<String, MutableLong> debugTiming = new LinkedHashMap<>();

//    @SuppressWarnings("rawtypes")
//    private Class<? extends FactFunction>[] functionClasses;
//    private final List<FactFunction<F>> functions = new ArrayList<>();
//    private final List<FactAggregator<F, ?>> aggregators = new ArrayList<>();
//    private FactFunction<F> factHandler;
//    private Set<Class<?>> dependencies;

    @Override
    public void initialize(R request, Context context, RuleSession session) {
//        super.doInitialize(objects);

//        enabled = config.getBoolean("investigator." + getClass().getSimpleName() + ".enabled", true);
        isDebug = LOG.isDebugEnabled();

        // Build the insert flow from the defined function classes
//        factHandler = buildFlow(objects);
    }

//    @SuppressWarnings({"unchecked", "rawtypes"})
//    private FactFunction<F> buildFlow(Object... objects) {
//        FactFunction<F> first = null;
//        FactFunction<F> current = null;
//        if (functionClasses != null) {
//            for (Class<? extends FactFunction> functionClass : functionClasses) {
//                try {
//                    FactFunction<F> f = ClassUtils.newInstance(functionClass);
//                    f.initialize(objects);
//                    functions.add(f);
//                    if (f instanceof FactAggregator) {
//                        aggregators.add((FactAggregator<F, ?>)f);
//                    }
//                    if (current != null) {
//                        current.andThen(f);
//                    } else {
//                        first = f;
//                    }
//                    current = f;
//                } catch (ClassUtils.UninstantiableClassException | RuntimeException e) {
//                    throw new RulesException(e, PROCESSOR_INSTANTIATION_FAILURE, functionClass.getSimpleName());
//                }
//            }
//        }
//
//        InsertFunction f = new InsertFunction();
//        if (current != null) {
//            current.andThen(f);
//        } else {
//            first = f;
//        }
//
//        return first;
//    }

//    @Override
//    public void release() {
//        super.release();
//
//        functions.forEach(Releasable::release);
//    }

//    @Override
//    public final void setOptions(InvestigatorFactory<R, F, ? extends Investigator<R, F>> factory, InvestigatorOptions options) {
//        setRequestClass(factory.getRequestClass());
//        factClass = factory.getFactClass();
//        if (options != null) {
//            functionClasses = options.functions();
//            dependencies = Arrays.stream(options.dependsOn()).collect(Collectors.toSet());
//        } else {
//            dependencies = Collections.emptySet();
//        }
//    }

//    @Override
//    public final Class<F> getFactClass() {
//        return factClass;
//    }

//    @Override
//    public boolean dependsOn(Collection<? extends Investigator<R, ?>> investigators) {
//        ArgumentUtilities.validateIfNotNull(investigators);
//        return !dependencies.isEmpty() && investigators.stream()
//                .map(Object::getClass)
//                .anyMatch(dependencies::contains);
//    }

    @Override
    public final void setSession(RuleSession session) {
//        ArgumentUtilities.validateIfNotNull(session);
        this.session = session;
    }

//    @Override
//    public void setTrace(Trace trace) {
//        parentTrace = trace;
//    }

    @Override
    @SuppressWarnings("squid:S1181")
    protected final void doRun() {
//        ThreadContext.put("Request", getContext().getId());
//        Trace trace = Trace.start(getName());
//        try {
//            if (parentTrace != null) {
//                 If a parent trace has been provided (from another Thread), attach to it
//                trace.attach(parentTrace);
//            }
            gatherFacts();
//        } catch (Throwable e) {
//            LOG.debug("Investigator '" + getName() + "' terminated by an exception", e);
//            if (getContext().isStopped() && ExceptionUtilities.containsTypeOf(e, InterruptedException.class)) {
                // If the run is cancelled externally, return from interruptions without complaint
                // Re-interrupt to terminate the arbiter, in case it is running single-threaded
//                Thread.currentThread().interrupt();
//                LOG.info(getName() + " was interrupted during investigation: " + ExceptionUtilities.getRootCause(e).getMessage());
//                return;
//            }
            // If not, allow the exception to propagate up
//            throw e;
//        } finally {
//            LOG.debug("Investigator '" + getName() + "' finished");
//            trace.stop();
//        }
    }

    @Override
    public final void gatherFacts() {
//        if (session == null) {
//            throw new IllegalStateException("Session must be set before facts can be gathered");
//        }
//        if (!enabled) {
//            LOG.info(getName() + " is disabled by configuration");
//            return;
//        }

//        Context context = getContext();
        context.startFacts(factClass);
        Trace.doWithTask("Before Gather", v -> beforeGather());
        Trace.doWithTask("Gather", v -> doGather());
        Trace.doWithTask("After Gather", v -> afterGather());
        LOG.info("Investigation complete");
        // Insert aggregated facts into the session and log the counts per class
        aggregators.stream()
                .flatMap(FactAggregator::getAggregationFacts)
                .peek(session::insert)
                .collect(Collectors.toMap(Object::getClass, key -> 1, Integer::sum))
                .forEach((clazz, count) -> {
                    LOG.info("- " + clazz.getSimpleName() + ": " + count + " aggregated facts");
                    context.finishFacts(clazz, count);
                });
        context.finishFacts(factClass, factCount);
        if (isDebug) {
            LOG.debug("Investigator " + getName() + " - " + FormatterUtilities.milliDurationAsString(context.getFactDuration(factClass)) + " gathering " + factClass.getSimpleName()
                    + " facts" + (debugTiming.isEmpty() ? "" : "\n" +
                    debugTiming.entrySet().stream()
                            .map(e -> " - " + e.getKey() + ": " + FormatterUtilities.nanoDurationToString(e.getValue()))
                            .collect(Collectors.joining("\n"))));
        }
    }

    /**
     * Inserts a Fact into the session after passing it through any defined functions
     *
     * @param fact the Fact to insert
     */
    protected final void insert(F fact) {
        if (fact != null) {
            startTiming("Session insert");
            factHandler.apply(fact);
            endTiming("Session insert");
        }
    }

    /**
     * Begin debug timing of the named operation
     *
     * @param name the operation name
     */
    @SuppressWarnings("WeakerAccess")
    protected void startTiming(String name) {
        if (isDebug) {
            MutableLong value = debugTiming.computeIfAbsent(name, k -> new MutableLong());
            value.set(value.longValue() - System.nanoTime());
        }
    }

    /**
     * End debug timing of the named operation
     *
     * @param name the operation name
     */
    @SuppressWarnings("WeakerAccess")
    protected void endTiming(String name) {
        if (isDebug) {
            MutableLong value = debugTiming.get(name);
            if (value != null) {
                value.set(value.longValue() + System.nanoTime());
            }
        }
    }

    /**
     * Overridden in subclasses to perform fact gathering tasks specific to this Investigator
     * <p>Subclasses must call insert() on each fact that they need to insert</p>
     */
    protected abstract void doGather();

    /**
     * Overridden in subclasses to execute code before fact gathering is begun
     */
    protected void beforeGather() {
    }

    /**
     * Overridden in subclasses to execute code after fact gathering is complete
     */
    protected void afterGather() {
    }

    /**
     * A FactFunction to insert facts into the RulesSession, typically the last step of the processing chain
     */
    private class InsertFunction extends AbstractFactFunction<F> {
        @Override
        public void apply(F fact) {
            session.insert(fact);
            ++factCount;
        }
    }
}
