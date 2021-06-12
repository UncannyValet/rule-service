package com.example.rules.spi.arbiter;

import com.spirent.cem.rules.api.RulesRequest;
import com.spirent.cem.rules.api.RulesResult;

import java.lang.annotation.*;

/**
 * An annotation which provides options for an Arbiter class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ArbiterOptions {

    /**
     * The RulesRequest class this Arbiter handles
     *
     * @deprecated request class extracted from Arbiter class signature
     * @return a RulesRequest class
     */
    @Deprecated
    Class<? extends RulesRequest> request() default RulesRequest.class;

    /**
     * The RulesResult class this Arbiter populates
     *
     * @deprecated result class extracted from Arbiter class signature
     * @return a RulesResult class
     */
    @Deprecated
    Class<? extends RulesResult> result() default RulesResult.class;

    /**
     * The session ID this Arbiter uses to retrieve a set of rules to run
     *
     * @return a session ID String
     */
    String rules() default "";
}
