package com.example.rules.spi.processor;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.annotation.Named;
import com.daxtechnologies.util.ArgumentUtilities;
import com.daxtechnologies.util.ClassUtils;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;

import static com.spirent.cem.rules.api.ErrorNumbers.PROCESSOR_INSTANTIATION_FAILURE;

/**
 * An abstract implementation of the RulesFactory interface, to minimize the effort required to implement this interface
 *
 * @param <P> the RulesProcessor class that this factory builds
 */
public abstract class AbstractRulesFactory<R extends RulesRequest, P extends RuleProcessor<R>> implements RuleFactory<R, P> {

    private final Class<P> processorClass;
    private final Class<R> requestClass;

    public AbstractRulesFactory(Class<P> processorClass) {
        ArgumentUtilities.validateIfNotNull(processorClass);
        this.processorClass = processorClass;
        requestClass = ClassUtils.getTypeArgument(processorClass, RuleProcessor.class, 0);
    }

    @Override
    public final P newProcessor() {
        try {
            P processor = ClassUtils.newInstance(processorClass);
            applyProcessorOptions(processor);
            return processor;
        } catch (ClassUtils.UninstantiableClassException e) {
            throw new RulesException(e, PROCESSOR_INSTANTIATION_FAILURE, processorClass.getName());
        }
    }

    @Override
    public void initialize(Object... objects) {
    }

    @Override
    public void release() {
    }

    @Override
    public String getName() {
        Named named = AnnotationUtilities.getAnnotation(processorClass, Named.class);
        if (named != null) {
            return named.value();
        }
        return processorClass.getSimpleName();
    }

    @Override
    public String getDescription() {
        return getName() + "/" + processorClass.getName();
    }

    @Override
    public Class<P> getProcessorClass() {
        return processorClass;
    }

    @Override
    public Class<R> getRequestClass() {
        return requestClass;
    }

    /**
     * Overridden in subclasses to process options from annotations to a constructed processor
     *
     * @param processor the process to apply options to
     */
    protected abstract void applyProcessorOptions(P processor);
}
