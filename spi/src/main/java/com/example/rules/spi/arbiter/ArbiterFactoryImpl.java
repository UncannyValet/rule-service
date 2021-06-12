package com.example.rules.spi.arbiter;

import com.daxtechnologies.util.ClassUtils;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;
import com.spirent.cem.rules.spi.processor.AbstractRulesFactory;

/**
 * An implementation of the ArbiterFactory interface
 *
 * @param <A> the AbstractArbiter class that this factory builds
 */
public class ArbiterFactoryImpl<R extends RulesRequest, O extends RulesResult, A extends Arbiter<R, O>> extends AbstractRulesFactory<R, A> implements ArbiterFactory<R, O, A> {

    private ArbiterOptions options;
    private final Class<O> resultClass;

    public ArbiterFactoryImpl(Class<A> processorClass) {
        super(processorClass);
        resultClass = ClassUtils.getTypeArgument(processorClass, Arbiter.class, 1);
    }

    @Override
    public void initialize(Object... objects) {
        super.initialize(objects);

        options = getProcessorClass().getAnnotation(ArbiterOptions.class);
    }

    @Override
    protected final void applyProcessorOptions(A arbiter) {
        arbiter.setOptions(this, options);
    }

    @Override
    public final Class<O> getResultClass() {
        return resultClass;
    }
}
