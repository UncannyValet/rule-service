package com.example.rules.spi.investigator;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.session.RuleSession;
import com.example.rules.spi.utils.ClassUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An abstract implementation of the Investigator interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the Fact class associated with this processor
 */
public abstract class AbstractInvestigator<R extends RuleRequest, F> implements Investigator<R, F> {

    @SuppressWarnings("squid:S00116")
    protected final Logger LOG = LogManager.getLogger(getClass());

    private static final Map<Class<?>, Set<Class<? extends Investigator<?, ?>>>> dependencyMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> factClasses = new ConcurrentHashMap<>();

    private final RuleContext context;
    private final Class<?> factClass;
    private final boolean isDebug;
    private final Map<String, MutableLong> debugTiming = new LinkedHashMap<>();
    private final Set<Class<? extends Investigator<?, ?>>> dependencies;

    private RuleSession session;
    private int factCount;

    public AbstractInvestigator(RuleContext context) {
        this.context = context;
        isDebug = LOG.isDebugEnabled();

        dependencies = dependencyMap.computeIfAbsent(getClass(), clazz -> {
            DependsUpon dependsUpon = clazz.getAnnotation(DependsUpon.class);
            if (dependsUpon != null) {
                Set<Class<? extends Investigator<?, ?>>> d = new HashSet<>();
                Collections.addAll(d, dependsUpon.value());
                return d;
            } else {
                return Collections.emptySet();
            }
        });

        factClass = factClasses.computeIfAbsent(getClass(), clazz -> ClassUtils.getTypeArgument(clazz, Investigator.class, 1));
    }

    @Override
    public final void gatherFacts(RuleSession session) {
        this.session = session;

        context.startFacts(factClass);
        time("Before Gather", v -> beforeGather());
        time("Gather", v -> doGather());
        time("After Gather", v -> afterGather());
        LOG.info("Investigation complete");
        context.finishFacts(factClass, factCount);
        if (isDebug) {
            LOG.debug("Investigator " + getClass() + " - " + context.getFactDuration(factClass) + " gathering " + factClass.getSimpleName()
                    + " facts" + (debugTiming.isEmpty() ? "" : "\n" +
                    debugTiming.entrySet().stream()
                            .map(e -> " - " + e.getKey() + ": " + e.getValue().longValue() / 1000 + " ms")
                            .collect(Collectors.joining("\n"))));
        }
    }

    /**
     * Inserts a Fact into the session after passing it through any defined functions
     *
     * @param fact the Fact to insert
     */
    protected final void insert(F fact) {
        fact = processFact(fact);
        if (fact != null) {
            session.insert(fact);
            ++factCount;
        }
    }

    /**
     * Process a fact after extraction before insert into the session
     * <br/>This method may return null if the resultant fact should not be inserted into the session
     */
    protected F processFact(F fact) {
        return fact;
    }

    @Override
    public final boolean dependsOn(Collection<? extends Investigator<R, ?>> investigators) {
        return !dependencies.isEmpty() && investigators.stream()
                .map(Object::getClass)
                .anyMatch(dependencies::contains);
    }

    protected final RuleContext getContext() {
        return context;
    }

    protected final void time(String name, Consumer<?> action) {
        if (isDebug) {
            try {
                startTiming(name);
                action.accept(null);
            } finally {
                endTiming(name);
            }
        } else {
            action.accept(null);
        }
    }

    /**
     * Begin debug timing of the named operation
     *
     * @param name the operation name
     */
    private void startTiming(String name) {
        MutableLong value = debugTiming.computeIfAbsent(name, k -> new MutableLong());
        value.setValue(value.longValue() - System.nanoTime());
    }

    /**
     * End debug timing of the named operation
     *
     * @param name the operation name
     */
    private void endTiming(String name) {
        MutableLong value = debugTiming.get(name);
        if (value != null) {
            value.setValue(value.longValue() + System.nanoTime());
        }
    }

    /**
     * Overridden in subclasses to perform fact gathering tasks specific to this Investigator
     * <p>Subclasses must call insert() on each fact that they need to insert</p>
     */
    protected abstract void doGather();

    /**
     * Overridden in subclasses to execute code before fact gathering begins
     */
    protected void beforeGather() {
    }

    /**
     * Overridden in subclasses to execute code after fact gathering is complete
     */
    protected void afterGather() {
    }
}
