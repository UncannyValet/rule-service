package com.example.rules.spi;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.feature.FeatureUtilities;
import com.daxtechnologies.feature.annotation.Feature;
import com.daxtechnologies.license.LicenseUtilities;
import com.daxtechnologies.license.annotation.License;
import com.daxtechnologies.oam.ILogger;
import com.daxtechnologies.oam.TheLogger;
import com.daxtechnologies.services.Services;
import com.daxtechnologies.util.ArgumentUtilities;
import com.daxtechnologies.util.ClassUtils;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;
import com.spirent.cem.rules.spi.arbiter.Arbiter;
import com.spirent.cem.rules.spi.arbiter.ArbiterFactory;
import com.spirent.cem.rules.spi.arbiter.ArbiterFactoryImpl;
import com.spirent.cem.rules.spi.investigator.Investigator;
import com.spirent.cem.rules.spi.investigator.InvestigatorFactory;
import com.spirent.cem.rules.spi.investigator.InvestigatorFactoryImpl;
import com.spirent.cem.rules.spi.processor.RulesFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.spirent.cem.rules.api.ErrorNumbers.*;

public class ProcessorRegistry {

    private static final ILogger LOG = TheLogger.getInstance(ProcessorRegistry.class);

    private final Map<Class<? extends RulesRequest>, ArbiterFactory<?, ?, ?>> arbiterFactories = new HashMap<>();
    private final Map<Class<? extends RulesRequest>, Set<InvestigatorFactory<?, ?, ?>>> investigatorFactories = new HashMap<>();

    /**
     * Retrieves the result class that will be provided for the given RulesRequest
     *
     * @param request the RulesRequest
     * @return the returned RulesResult class, or {@code null} if none is round
     */
    public final Class<? extends RulesResult> getResultClass(RulesRequest request) {
        ArgumentUtilities.validateIfNotNull(request);
        return getResultClass(request.getClass());
    }

    /**
     * Retrieves the result class that will be provided for the given RulesRequest class
     *
     * @param requestClass the RulesRequest class
     * @return the returned RulesResult class, or {@code null} if none is round
     */
    public final Class<? extends RulesResult> getResultClass(Class<? extends RulesRequest> requestClass) {
        ArgumentUtilities.validateIfNotNull(requestClass);

        ArbiterFactory<?, ?, ?> factory = arbiterFactories.get(requestClass);
        return factory == null ? null : factory.getResultClass();
    }

    /**
     * Gets a new Investigator to gather facts for a given request and fact class
     *
     * @param request   the RulesRequest to process
     * @param factClass the class of Fact to gather
     * @return a new Investigator matching the request and fact class, throwing an exception if none is found
     */
    public <R extends RulesRequest, F> Investigator<R, F> getInvestigator(R request, Class<F> factClass) {
        return getInvestigatorFactories(request)
                .filter(f -> factClass.equals(f.getFactClass()))
                .map(f -> {
                    @SuppressWarnings("unchecked")
                    InvestigatorFactory<R, F, Investigator<R, F>> factory = (InvestigatorFactory<R, F, Investigator<R, F>>)f;
                    return factory;
                })
                .map(RulesFactory::newProcessor)
                .findAny()
                .orElseThrow(() -> new RulesException(INVESTIGATOR_NOT_REGISTERED, request.getName(), factClass.getSimpleName()));
    }

    /**
     * Gets a Collection of Investigators to gather facts for a given request
     *
     * @param request the RulesRequest to process
     * @return a Collection of Investigators matching the request
     */
    public <R extends RulesRequest> Collection<Investigator<R, ?>> getInvestigators(R request) {
        return getInvestigatorFactories(request)
                .map(RulesFactory::newProcessor)
                .map(o -> (Investigator<R, ?>)o)
                .collect(Collectors.toList());
    }

    /**
     * Gets a Stream of enabled (Feature and License) investigator factories matching the given request
     *
     * @param request the RulesRequest to process
     * @return a Stream of enabled Investigators matching the request
     */
    private <R extends RulesRequest> Stream<InvestigatorFactory<R, ?, ? extends Investigator<R, ?>>> getInvestigatorFactories(R request) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<InvestigatorFactory<R, ?, ? extends Investigator<R, ?>>> factories = (Set)this.investigatorFactories.get(request.getClass());
        return factories == null ? Stream.empty() : factories.stream()
                .filter(f -> FeatureUtilities.hasFeaturesFor(f.getProcessorClass()))
                .filter(f -> LicenseUtilities.isLicensed(f.getProcessorClass()));
    }

    private <R extends RulesRequest, A extends Arbiter<R, ? extends RulesResult>> ArbiterFactory<R, ?, A> getArbiterFactory(Class<R> requestClass) {
        @SuppressWarnings("unchecked")
        ArbiterFactory<R, ?, A> factory = (ArbiterFactory<R, ?, A>)arbiterFactories.get(requestClass);
        if (factory == null) {
            throw new RulesException(ARBITER_NOT_REGISTERED, requestClass);
        }
        return factory;
    }

    /**
     * Gets the Arbiter class that handles the given request type
     *
     * @param requestClass the RulesRequest class
     * @return a non-null Arbiter class
     */
    public <R extends RulesRequest> Class<? extends Arbiter<R, ? extends RulesResult>> getArbiterClass(Class<R> requestClass) {
        return getArbiterFactory(requestClass).getProcessorClass();
    }

    /**
     * Gets a new Arbiter to process a given request
     *
     * @param request the RulesRequest to process
     * @return a new Arbiter matching the request, throwing an exception if none is found
     */
    @SuppressWarnings("squid:S1905")
    public <R extends RulesRequest, A extends Arbiter<R, ? extends RulesResult>> A getArbiter(R request) {
        @SuppressWarnings("unchecked")
        Class<R> requestClass = (Class<R>)request.getClass();
        ArbiterFactory<R, ?, A> factory = getArbiterFactory(requestClass);
        if (!FeatureUtilities.hasFeaturesFor(factory.getProcessorClass())) {
            Feature features = AnnotationUtilities.getAnnotation(factory.getProcessorClass(), Feature.class);
            throw new RulesException(ARBITER_NO_FEATURE, request.getName(), features == null ? "?" : features.value());
        }
        if (!LicenseUtilities.isLicensed(factory.getProcessorClass())) {
            License features = AnnotationUtilities.getAnnotation(factory.getProcessorClass(), License.class);
            throw new RulesException(ARBITER_NO_FEATURE, request.getName(), features == null ? "?" : features.value());
        }
        return factory.newProcessor();
    }

    /**
     * Registers Investigator and Arbiter implementations found in the classpath
     */
    public void initialize() {
        registerArbiterFactories();
        registerInvestigatorFactories();
    }

    /**
     * Registers all Investigators
     */
    @SuppressWarnings("unchecked")
    private void registerInvestigatorFactories() {
        LOG.info("Prepare Investigator factories...");
        Services.resolveProviders(Investigator.class).forEach(clazz -> {
            if (ClassUtils.canInstantiate(clazz)) {
                try {
                    registerInvestigatorFactory(clazz);
                } catch (RuntimeException e) {
                    LOG.error("Failed to register investigator factory for " + clazz.getName(), e);
                }
            }
        });
    }

    /**
     * Registers all Arbiters
     */
    @SuppressWarnings("unchecked")
    private void registerArbiterFactories() {
        LOG.info("Prepare Arbiter factories...");
        Services.resolveProviders(Arbiter.class).forEach(clazz -> {
            if (ClassUtils.canInstantiate(clazz)) {
                try {
                    registerArbiterFactory(clazz);
                } catch (RuntimeException e) {
                    LOG.error("Failed to register arbiter factory for " + clazz.getName(), e);
                }
            }
        });
    }

    /**
     * Registers an InvestigatorFactory for later use.
     *
     * @param investigatorClass the Investigator Class to register
     */
    private <R extends RulesRequest, F, I extends Investigator<R, F>> void registerInvestigatorFactory(Class<I> investigatorClass) {
        InvestigatorFactoryImpl<R, F, I> factory = new InvestigatorFactoryImpl<>(investigatorClass);
        factory.initialize((Object)null);
        Class<R> requestClass = factory.getRequestClass();
        investigatorFactories.computeIfAbsent(requestClass, c -> new HashSet<>()).add(factory);
        LOG.info("- " + factory.getName() + " provides '" + factory.getFactClass().getSimpleName() + "' for request '" + requestClass.getSimpleName() + "'");
    }

    /**
     * Registers an ArbiterFactory for later use.
     *
     * @param arbiterClass the Arbiter Class to register
     */
    private <R extends RulesRequest, O extends RulesResult, A extends Arbiter<R, O>> void registerArbiterFactory(Class<A> arbiterClass) {
        ArbiterFactoryImpl<R, O, A> factory = new ArbiterFactoryImpl<>(arbiterClass);
        factory.initialize((Object)null);
        String requestClass = factory.getRequestClass().getSimpleName();
        ArbiterFactory<?, ?, ?> oldFactory = arbiterFactories.put(factory.getRequestClass(), factory);
        if (oldFactory != null) {
            LOG.warn("Request class " + requestClass + " already registered by " + oldFactory.getName());
        }
        LOG.info("- " + factory.getName() + " handles request class " + requestClass);
    }
}
