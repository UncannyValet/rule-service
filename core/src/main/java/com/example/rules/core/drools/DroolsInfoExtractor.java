package com.example.rules.core.drools;

import com.example.rules.api.RuleInfo;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility function to extract a Stream of RuleInfo from a KieContainer
 */
public class DroolsInfoExtractor implements Function<KieContainer, Stream<RuleInfo>> {

    private static final InfoMapper<?>[] MAPPERS = new InfoMapper[]{
            new FactMapper(),
            new GlobalMapper(),
            new FunctionMapper(),
            new QueryMapper(),
            new RuleMapper()
    };

    @Override
    public Stream<RuleInfo> apply(KieContainer container) {
        return container.getKieBaseNames().stream()
                .map(container::getKieSessionNamesInKieBase)
                .flatMap(Collection::stream)
                .flatMap(id -> {
                    ReleaseId releaseId = container.getReleaseId();
                    String source = releaseId == null ? "-" : releaseId.toString();
                    KieSession session = container.newKieSession(id);

                    return session.getKieBase().getKiePackages().stream()
                            .filter(pkg -> !pkg.getRules().isEmpty())
                            .flatMap(pkg -> {
                                String pkgName = pkg.getName();
                                return Stream.of(MAPPERS)
                                        .map(mapper -> mapper.apply(pkg))
                                        .flatMap(s -> s)
                                        .peek(i -> {
                                            i.setPkg(pkgName);
                                            i.setSource(source);
                                            i.setSession(id);
                                        });
                            });
                });
    }

    private abstract static class InfoMapper<T> implements Function<KiePackage, Stream<RuleInfo>> {

        private final RuleInfo.Type type;
        private final Function<KiePackage, Collection<T>> items;
        private final Function<T, String> name;
        private final Function<T, Map<String, Object>> attributes;

        InfoMapper(RuleInfo.Type type, Function<KiePackage, Collection<T>> itemExtractor, Function<T, String> nameExtractor, Function<T, Map<String, Object>> attributeExtractor) {
            this.type = type;
            this.items = itemExtractor;
            this.name = nameExtractor;
            this.attributes = attributeExtractor;
        }

        @Override
        public Stream<RuleInfo> apply(KiePackage pkg) {
            return items.apply(pkg).stream()
                    .map(f -> {
                        RuleInfo info = new RuleInfo();
                        info.setType(type);
                        info.setId(name.apply(f));
                        info.setAttributes(attributes.apply(f));
                        return info;
                    });
        }
    }

    private static class FactMapper extends InfoMapper<FactType> {
        FactMapper() {
            super(RuleInfo.Type.FACT,
                    KiePackage::getFactTypes,
                    FactType::getName,
                    f -> {
                        Map<String, Object> attributes = f.getFields().stream().collect(Collectors.toMap(FactField::getName, field -> field.getType().getName()));
                        Map<String, Object> metadata = f.getMetaData();
                        if (metadata != null) {
                            attributes.putAll(metadata);
                        }
                        return attributes;
                    });
        }
    }

    private static class GlobalMapper extends InfoMapper<Global> {
        GlobalMapper() {
            super(RuleInfo.Type.GLOBAL,
                    KiePackage::getGlobalVariables,
                    Global::getName,
                    f -> Stream.of(f).collect(Collectors.toMap(i -> "Type", i -> i.getType())));
        }
    }

    private static class FunctionMapper extends InfoMapper<String> {
        FunctionMapper() {
            super(RuleInfo.Type.FUNCTION,
                    KiePackage::getFunctionNames,
                    s -> s,
                    f -> Collections.emptyMap());
        }
    }

    private static class QueryMapper extends InfoMapper<Query> {
        QueryMapper() {
            super(RuleInfo.Type.QUERY,
                    KiePackage::getQueries,
                    Query::getName,
                    Query::getMetaData);
        }
    }

    private static class RuleMapper extends InfoMapper<Rule> {
        RuleMapper() {
            super(RuleInfo.Type.RULE,
                    KiePackage::getRules,
                    Rule::getName,
                    Rule::getMetaData);
        }
    }
}
