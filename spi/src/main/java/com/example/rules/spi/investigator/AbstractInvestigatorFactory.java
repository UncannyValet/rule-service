package com.example.rules.spi.investigator;

//import com.daxtechnologies.util.ClassUtils;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.utils.ClassUtils;
//import com.example.rules.spi.processor.AbstractRulesFactory;

/**
 * An implementation of the InvestigatorFactory interface
 *
 * @param <I> the Investigator class that this factory builds
 */
public abstract class AbstractInvestigatorFactory<R extends RuleRequest, F, I extends Investigator<R, F>> /*extends AbstractRulesFactory<R, I>*/ implements InvestigatorFactory<R, F, I> {

    private final Class<I> investigatorClass;
    private final Class<R> requestClass;
    private final Class<F> factClass;

    protected AbstractInvestigatorFactory() {
        investigatorClass = ClassUtils.getTypeArgument(getClass(), AbstractInvestigatorFactory.class, 3);
        requestClass = ClassUtils.getTypeArgument(investigatorClass, Investigator.class, 1);
        factClass = ClassUtils.getTypeArgument(investigatorClass, Investigator.class, 2);
    }

    @Override
    public Class<R> getRequestClass() {
        return requestClass;
    }

    @Override
    public final Class<F> getFactClass() {
        return factClass;
    }

    @Override
    public I newInvestigator() {
        return ClassUtils.instantiate(investigatorClass);
    }
}
