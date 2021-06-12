package com.example.rules.spi.investigator;

/**
 * <p>This class provides a skeletal implementation of the {@link FactAggregator}
 * interface, to minimize the effort required to implement this interface</p>
 */
public abstract class AbstractFactAggregator<F, T> extends AbstractFactFunction<F> implements FactAggregator<F, T> {

    @Override
    public void apply(F fact) {
        accept(fact);
        next.apply(fact);
    }
}
