package com.example.rules.api;

/**
 * A marker interface for a rule run request.  Implementations should define all
 * the information needed for the rule engine to gather facts and execute rules against them.
 * <p>Implementations should also override hashCode() and equals() methods to allow the service
 * to find previous rule runs that were executed against a particular request.</p>
 */
public interface RuleRequest {

    enum State {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILURE,
        CANCELED
    }
}
