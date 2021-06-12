package com.example.rules.spi.investigator;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A FactFunction to aggregate facts as they are gathered
 *
 * @param <F> the fact type
 * @param <T> the aggregated object type
 */
public interface FactAggregator<F, T> extends FactFunction<F>, Consumer<F> {

    Stream<T> getAggregationFacts();
}
