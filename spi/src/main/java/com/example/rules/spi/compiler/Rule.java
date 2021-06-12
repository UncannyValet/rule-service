package com.example.rules.spi.compiler;

public interface Rule {

    /**
     * Returns the ID of the Rule
     *
     * @return the ID
     */
    String getId();

    /**
     * Returns the compiled text of the rule
     *
     * @return the Rule text
     */
    String getText();

    interface Builder {

        /**
         * Defines an attribute of the Rule
         *
         * @param name  the name of the attribute
         * @param value the value of the attribute
         * @return this
         */
        Builder attribute(String name, String value);

        /**
         * Defines the triggering condition of the Rule
         *
         * @param condition the triggering condition
         * @return this
         */
        Builder when(String condition);

        /**
         * Defines the execution steps of the Rule
         *
         * @param statement the execution steps
         * @return this
         */
        Builder then(String statement);

        /**
         * Compiles the Rule
         *
         * @return a new Rule
         */
        Rule build();
    }
}
