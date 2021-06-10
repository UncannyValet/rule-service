package com.example.rules.api;

//import com.daxtechnologies.exception.BaseRuntimeException;
//import com.daxtechnologies.exception.Module;

/**
 * Generic exception for all rules engine failures
 */
public class RulesException extends RuntimeException {

    private static final Object[] EMPTY_ARGUMENTS = new Object[]{};

    private final int errorNumber;
    private final Object[] arguments;

    public RulesException(int errorNumber) {
        this(errorNumber, EMPTY_ARGUMENTS);
    }

    public RulesException(int errorNumber, Object... arguments) {
        super();
        this.errorNumber = errorNumber;
        this.arguments = arguments;
    }

    public RulesException(Throwable cause, int errorNumber) {
        this(cause, errorNumber, EMPTY_ARGUMENTS);
    }

    public RulesException(Throwable cause, int errorNumber, Object... arguments) {
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
