package com.example.rules.spi.investigator;

/**
 * <p>This class provides a skeletal implementation of the {@link FactMutator}
 * interface, to minimize the effort required to implement this interface</p>
 */
public abstract class AbstractFactMutator<F> extends AbstractFactFunction<F> implements FactMutator<F> {

    @Override
    public void apply(F fact) {
        accept(fact);
        next.apply(fact);
    }
}
