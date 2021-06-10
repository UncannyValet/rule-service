package com.example.rules.api;

public class ErrorNumbers {

    public static final int UNKNOWN = 0;
    public static final int ARBITER_NOT_REGISTERED = 1;
    public static final int INVESTIGATOR_NOT_REGISTERED = 2;
    public static final int ARBITER_MISSING_OPTIONS = 3;
    public static final int INVESTIGATOR_MISSING_OPTIONS = 4;
    public static final int DESERIALIZATION_ERROR = 5;
    public static final int SERIALIZATION_ERROR = 6;
    public static final int RESULT_INSTANTIATION = 7;
    public static final int INVESTIGATORS_NOT_REGISTERED = 8;
    public static final int FACT_INSTANTIATION = 9;
    public static final int NO_SQL_EXTRACTOR = 10;
    public static final int SQL_EXTRACT_FAILURE = 11;
    public static final int MISSING_PROCESSOR_CLASS = 12;
    public static final int BAD_PROCESSOR_CLASS = 13;
    public static final int PROCESSOR_INSTANTIATION_FAILURE = 14;
    public static final int NO_PROCESSOR_REQUEST = 15;
    public static final int NO_PROCESSOR_CONTEXT = 16;
    public static final int UNKNOWN_SESSION = 17;
    public static final int UNKNOWN_RULES_TYPE = 18;
    public static final int INVALID_FACT_TYPE = 19;
    public static final int INVALID_SUCCESS_CALLBACK = 20;
    public static final int CLI_FAILURE = 21;
    public static final int INVESTIGATOR_FAILURE = 22;
    public static final int INVALID_DIMENSION = 23;
    public static final int DUPLICATE_CONTAINER = 24;
    public static final int ARBITER_NO_FEATURE = 25;
    public static final int COMPILATION_FAILURE = 26;

    public static final int PROCESS_FAILURE = 100;

    private ErrorNumbers() {
    }
}
