package com.example.rules.spi.investigator;

import com.daxtechnologies.util.ClassUtils;
import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.spi.processor.AbstractRulesFactory;

/**
 * An implementation of the InvestigatorFactory interface
 *
 * @param <I> the AbstractInvestigator class that this factory builds
 */
public class InvestigatorFactoryImpl<R extends RulesRequest, F, I extends Investigator<R, F>> extends AbstractRulesFactory<R, I> implements InvestigatorFactory<R, F, I> {

    private InvestigatorOptions options;
    private final Class<F> factClass;

    public InvestigatorFactoryImpl(Class<I> processorClass) {
        super(processorClass);

        factClass = ClassUtils.getTypeArgument(processorClass, Investigator.class, 1);
    }

    @Override
    public void initialize(Object... objects) {
        super.initialize(objects);

        options = getProcessorClass().getAnnotation(InvestigatorOptions.class);
    }

    @Override
    protected final void applyProcessorOptions(I investigator) {
        investigator.setOptions(this, options);
    }

    @Override
    public final Class<F> getFactClass() {
        return factClass;
    }
}
