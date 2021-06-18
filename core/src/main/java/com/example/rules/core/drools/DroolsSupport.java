package com.example.rules.core.drools;

import org.drools.decisiontable.DecisionTableProviderImpl;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DroolsSupport {

    private DroolsSupport() {
    }

    /**
     * Translates a Drools decision table in Excel format to its equivalent DRL
     *
     * @param resource a Resource pointing to a decision table in XLS format
     * @return a String containing the equivalent DRL
     */
    @SuppressWarnings("WeakerAccess")
    public static String compileDecisionTable(Resource resource) {
        try {
            org.kie.api.io.Resource kieResource = ResourceFactory.newInputStreamResource(resource.getInputStream());
            DecisionTableProviderImpl decisionTableProvider = new DecisionTableProviderImpl();
            return decisionTableProvider.loadFromResource(kieResource, KnowledgeBuilderFactory.newDecisionTableConfiguration());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
