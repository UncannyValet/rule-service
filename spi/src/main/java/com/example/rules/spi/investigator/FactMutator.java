package com.example.rules.spi.investigator;

import java.util.function.Consumer;

/**
 * A FactFunction to modify facts as they are gathered
 *
 * @param <F> the fact type
 */
public interface FactMutator<F> extends FactFunction<F>, Consumer<F> {
}
