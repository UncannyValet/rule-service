package com.example.rules.api;

/**
 * Generic exception for all rule engine failures
 */
public class RuleException extends RuntimeException {

    private static final Object[] EMPTY_ARGUMENTS = new Object[]{};

    private final int errorNumber;
    private final Object[] arguments;

    public RuleException(int errorNumber) {
        this(errorNumber, EMPTY_ARGUMENTS);
    }

    public RuleException(int errorNumber, Object... arguments) {
        super();
        this.errorNumber = errorNumber;
        this.arguments = arguments;
    }

    public RuleException(Throwable cause, int errorNumber) {
        this(cause, errorNumber, EMPTY_ARGUMENTS);
    }

    public RuleException(Throwable cause, int errorNumber, Object... arguments) {
        super(cause);
        this.errorNumber = errorNumber;
        this.arguments = arguments;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
