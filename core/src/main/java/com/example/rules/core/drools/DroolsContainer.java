package com.example.rules.core.drools;

import com.example.rules.api.RuleInfo;
import com.example.rules.spi.session.RuleContainer;
import com.example.rules.spi.session.RuleSession;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DroolsContainer implements RuleContainer {

    private final KieContainer kieContainer;
    private final String id;

    public DroolsContainer(KieContainer container) {
        kieContainer = container;
        ReleaseId releaseId = kieContainer.getReleaseId();
        id = releaseId != null ? releaseId.getGroupId() + ":" + releaseId.getArtifactId() : "Drools Classpath";
    }

    public KieContainer getKieContainer() {
        return kieContainer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getProvidedSessions() {
        return kieContainer.getKieBaseNames().stream()
                .map(kieContainer::getKieSessionNamesInKieBase)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public RuleSession newSession(String sessionId) {
        KieSession kieSession = kieContainer.newKieSession(sessionId);
        return kieSession != null ? new DroolsSession(kieSession) : null;
    }

    @Override
    public Stream<RuleInfo> getRuleInfo() {
        return new DroolsInfoExtractor().apply(kieContainer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroolsContainer that = (DroolsContainer)o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
