package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.utils.ClassUtils;

/**
 * An implementation of the ArbiterFactory interface
 *
 * @param <A> the AbstractArbiter class that this factory builds
 */
public abstract class AbstractArbiterFactory<R extends RuleRequest, O extends RuleResult, A extends Arbiter<R, O>> /*extends AbstractRulesFactory<R, A>*/ implements ArbiterFactory<R, O, A> {

    private final Class<A> arbiterClass;
    private final Class<R> requestClass;
    private final Class<O> resultClass;

    protected AbstractArbiterFactory() {
        arbiterClass = ClassUtils.getTypeArgument(getClass(), AbstractArbiterFactory.class, 3);
        requestClass = ClassUtils.getTypeArgument(arbiterClass, Arbiter.class, 1);
        resultClass = ClassUtils.getTypeArgument(arbiterClass, Arbiter.class, 2);
    }

    @Override
    public Class<R> getRequestClass() {
        return requestClass;
    }

    @Override
    public final Class<O> getResultClass() {
        return resultClass;
    }

    @Override
    public A newArbiter() {
        return ClassUtils.instantiate(arbiterClass);
    }
}
