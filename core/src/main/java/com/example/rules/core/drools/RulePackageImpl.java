package com.example.rules.core.drools;

import com.example.rules.api.RuleException;
import com.example.rules.spi.compiler.Rule;
import com.example.rules.spi.compiler.RulePackage;
import com.example.rules.spi.session.RuleSession;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.rules.api.ErrorNumbers.COMPILATION_FAILURE;

public class RulePackageImpl implements RulePackage {

    private final KieContainer container;
    private final String sessionId;

    private RulePackageImpl(KieContainer container, String sessionId) {
        this.container = container;
        this.sessionId = sessionId;
    }

    @Override
    public String getId() {
        return container.getReleaseId().toExternalForm();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public RuleSession getSession() {
        return new DroolsSession(container.newKieSession());
    }

    public static class Builder implements RulePackage.Builder {

        private final String pkg;
        private final String sessionId;
        private final Set<Class<?>> imports = new HashSet<>();
        private final Map<String, Class<?>> globals = new HashMap<>();
        private final Map<String, Rule> rules = new HashMap<>();

        public Builder(String pkg, String sessionId) {
            this.pkg = pkg;
            this.sessionId = sessionId;
        }

        @Override
        public Builder addImport(Class<?> clazz) {
            imports.add(clazz);
            return this;
        }

        @Override
        public Builder global(String name, Class<?> clazz) {
            globals.put(name, clazz);
            return this;
        }

        @Override
        public Builder rule(Rule rule) {
            rules.put(rule.getId(), rule);
            return this;
        }

        @Override
        public List<String> validate() {
            ReleaseId release = KieServices.get().newReleaseId(pkg, "validation", "1.0.0");
            List<Message> messages = compile(release).getMessages();
            return messages.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        @Override
        public RulePackage build() {
            KieServices kieServices = KieServices.get();

            ReleaseId releaseId = kieServices.newReleaseId(pkg, "compiled", "1.0.0");

            Results results = compile(releaseId);

            if (!results.getMessages().isEmpty()) {
                throw new RuleException(COMPILATION_FAILURE, pkg, results.getMessages());
            }

            KieContainer container = kieServices.newKieContainer(releaseId);

            return new RulePackageImpl(container, sessionId);
        }

        private Results compile(ReleaseId release) {
            KieServices kieServices = KieServices.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();

            kfs.generateAndWritePomXML(release);

            KieModuleModel module = kieServices.newKieModuleModel();
            KieBaseModel base = module.newKieBaseModel()
                    .addPackage(pkg)
                    .setEventProcessingMode(EventProcessingOption.CLOUD)
                    .setEqualsBehavior(EqualityBehaviorOption.IDENTITY);

            base.newKieSessionModel(sessionId)
                    .setType(KieSessionModel.KieSessionType.STATEFUL)
                    .setDefault(true);
            kfs.writeKModuleXML(module.toXML());

            String dirName = pkg.replace(".", "/");

            // DRL for imports and globals
            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(pkg).append(";\n");
            imports.forEach(i -> sb.append("import ").append(i.getName()).append(";\n"));
            globals.forEach((name, clazz) -> sb.append("global ").append(clazz.getName()).append(' ').append(name).append(";\n"));
            kfs.write("src/main/resources/" + dirName + "/_imports.drl", sb.toString());

            // Separate DRL for each rule
            rules.forEach((id, rule) -> kfs.write("src/main/resources/" + dirName + "/" + id + ".drl",
                    "package " + pkg + ";\n" + rule.getText()));

            KieBuilder builder = kieServices.newKieBuilder(kfs);
            builder.buildAll();
            return builder.getResults();
        }
    }
}
