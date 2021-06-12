package com.example.rules.spi.processor;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.annotation.Named;
import com.daxtechnologies.configuration.Configuration;
import com.daxtechnologies.configuration.ConfigurationFactory;
import com.daxtechnologies.services.trace.Trace;
import com.daxtechnologies.services.worker.AbstractWorker;
import com.daxtechnologies.util.ObjectUtilities;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.spi.Context;

import static com.spirent.cem.rules.api.ErrorNumbers.NO_PROCESSOR_CONTEXT;
import static com.spirent.cem.rules.api.ErrorNumbers.NO_PROCESSOR_REQUEST;

/**
 * An abstract implementation of the RulesProcessor interface, to minimize the effort required to implement this interface
 *
 * @param <R> the RulesRequest class associated with this processor
 */
public abstract class AbstractRulesProcessor<R extends RulesRequest> extends AbstractWorker implements RuleProcessor<R> {

    private R request;
    private Class<R> requestClass;
    private Context rulesContext;

    protected final Configuration config = ConfigurationFactory.getInstance().getConfiguration().subset("rules");

    @Override
    public final void initialize(Object... objects) {
        Trace.doWithTask("Initialization (" + getName() + ")", v -> {
            // Initialized with the RulesRequest and context
            request = ObjectUtilities.findInstance(objects, getRequestClass());
            if (request == null) {
                throw new RulesException(NO_PROCESSOR_REQUEST, getRequestClass().getName(), getName());
            }

            rulesContext = ObjectUtilities.findInstance(objects, Context.class);
            if (rulesContext == null) {
                throw new RulesException(NO_PROCESSOR_CONTEXT, getName());
            }

            doInitialize(objects);
        });
    }

    protected void doInitialize(Object... objects) {
    }

    @Override
    public final Class<R> getRequestClass() {
        return requestClass;
    }

    @Override
    public void release() {
    }

    @Override
    public String getName() {
        Named named = AnnotationUtilities.getAnnotation(getClass(), Named.class);
        if (named != null) {
            return named.value();
        }
        return getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return getName() + "/" + getClass().getName();
    }

    /**
     * Gets the RulesRequest this processor was initialized with
     *
     * @return the RulesRequest
     */
    protected final R getRequest() {
        return request;
    }

    /**
     * Gets the Context this processor was initialized with
     *
     * @return the Context
     */
    protected final Context getContext() {
        return rulesContext;
    }

    /**
     * Called to set the expected RulesRequest class, used to retrieve the request during initialization
     *
     * @param clazz the RulesRequest class
     */
    protected final void setRequestClass(Class<R> clazz) {
        requestClass = clazz;
    }
}
