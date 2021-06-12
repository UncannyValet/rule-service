package com.example.rules.spi.investigator;

/**
 * <p>This class provides a skeletal implementation of the {@link FactFilter}
 * interface, to minimize the effort required to implement this interface</p>
 */
public abstract class AbstractFactFilter<F> extends AbstractFactFunction<F> implements FactFilter<F> {

    @Override
    public void apply(F fact) {
        if (test(fact)) {
            next.apply(fact);
        }
    }
}
