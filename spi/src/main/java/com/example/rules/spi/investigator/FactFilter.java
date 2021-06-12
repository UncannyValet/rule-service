package com.example.rules.spi.investigator;

import java.util.function.Predicate;

/**
 * A FactFunction to filter records as they are gathered
 *
 * @param <F> the fact type
 */
public interface FactFilter<F> extends FactFunction<F>, Predicate<F> {
}
