package com.example.rules.spi.investigator;

import com.daxtechnologies.util.Initializable;

/**
 * A basic object to operate on facts as they are gathered
 *
 * @param <F> the fact type
 */
public interface FactFunction<F> extends Initializable {

    /**
     * Assigns a function to be executed after this one completes
     */
    default void andThen(FactFunction<F> next) {
    }

    /**
     * Applies this function to the fact before passing it to the next function in the chain
     */
    void apply(F fact);
}
