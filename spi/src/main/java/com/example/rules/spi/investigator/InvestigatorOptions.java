package com.example.rules.spi.investigator;

import com.spirent.cem.rules.api.RulesRequest;

import java.lang.annotation.*;

/**
 * An annotation which provides options for an Investigator class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface InvestigatorOptions {

    /**
     * The RulesRequest class this Investigator handles
     *
     * @deprecated request class extracted from Investigator class signature
     * @return a RulesRequest class
     */
    @Deprecated
    Class<? extends RulesRequest> request() default RulesRequest.class;

    /**
     * The Fact class this investigator gathers
     *
     * @deprecated fact class extracted from Investigator class signature
     * @return a Fact class
     */
    @Deprecated
    Class<?> fact() default Object.class;

    /**
     * A set of function classes to be applied to the facts before insertion to allow for filtering, modification and aggregation.
     *
     * @return a set of function classes
     */
    Class<? extends FactFunction>[] functions() default {};

    /**
     * A set of investigators that this investigator depends on
     * <p>If these investigators are included in an investigation, this one will
     * wait until those have finished loading facts before continuing</p>
     */
    Class<? extends Investigator>[] dependsOn() default {};
}
