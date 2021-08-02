package com.example.rules.api;

/**
 * Generic exception for all rule engine failures
 */
public class RuleException extends RuntimeException {

    public RuleException(String message) {
        super(message);
    }

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
