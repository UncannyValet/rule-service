package com.example.rules.core.processor;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.investigator.Investigator;
import com.example.rules.spi.utils.ClassUtils;
import lombok.Setter;
import org.atteo.classindex.ClassIndex;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InvestigatorFactoryImpl implements InvestigatorFactory, ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends RuleRequest>, Set<Class<? extends Investigator>>> investigatorMap = new HashMap<>();

    public InvestigatorFactoryImpl() {
        ClassIndex.getSubclasses(Investigator.class).forEach(c -> {
            if (ClassUtils.canInstantiate(c)) {
                Class<? extends RuleRequest> requestClass = ClassUtils.getTypeArgument(c, Investigator.class, 0);
                investigatorMap.computeIfAbsent(requestClass, k -> new HashSet<>()).add(c);
            }
        });
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R extends RuleRequest> Collection<Investigator<R, ?>> getInvestigators(RuleContext context) {
        Set<Class<? extends Investigator>> investigatorClasses = investigatorMap.get(context.getRequest().getClass());
        if (investigatorClasses != null) {
            return investigatorClasses.stream()
                    .map(c -> (Class<? extends Investigator<R, ?>>)c)
                    .map(c -> applicationContext.getBean(c, context))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptySet();
        }
    }
}
