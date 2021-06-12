package com.example.rules.spi.compiler;

import com.example.rules.spi.session.RulesSession;

import java.util.List;

public interface RulePackage {

    /**
     * Returns the fully-qualified ID of the package
     *
     * @return the ID
     */
    String getId();

    /**
     * Returns the Session ID contained in the package
     *
     * @return the Session ID
     */
    String getSessionId();

    /**
     * Returns a RulesSession containing the rules defined in the package
     *
     * @return a new RulesSession
     */
    RulesSession getSession();

    interface Builder {

        /**
         * Adds a Class to be used in any added rules
         *
         * @param clazz the Class to import
         * @return this
         */
        Builder addImport(Class<?> clazz);

        /**
         * Adds a global variable to the package
         *
         * @param name  the name of the global variable
         * @param clazz the Class of the global variable
         * @return this
         */
        Builder global(String name, Class<?> clazz);

        /**
         * Adds a Rule to the package
         *
         * @param rule the Rule
         * @return this
         */
        Builder rule(Rule rule);

        /**
         * Checks the validity of the package and reports compilation issues, if any
         *
         * <p/>Compilation is successful if no issues are reported (the List is empty)
         */
        List<String> validate();

        /**
         * Compiles the package
         *
         * @return a new RulePackage
         */
        RulePackage build();
    }
}
