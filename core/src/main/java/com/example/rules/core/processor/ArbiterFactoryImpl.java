package com.example.rules.core.processor;

import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.utils.ClassUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.atteo.classindex.ClassIndex;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Component
public class ArbiterFactoryImpl implements ArbiterFactory, ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends RuleRequest>, Class<? extends Arbiter>> arbiterMap = new HashMap<>();
    private final Map<Class<? extends RuleRequest>, Class<? extends Serializable>> resultMap = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public ArbiterFactoryImpl() {
        ClassIndex.getSubclasses(Arbiter.class).forEach(c -> {
            if (ClassUtils.canInstantiate(c)) {
                Class<? extends RuleRequest> requestClass = ClassUtils.getTypeArgument(c, Arbiter.class, 0);
                Class<? extends Arbiter> previous = arbiterMap.putIfAbsent(requestClass, c);
                if (previous != null) {
                    log.warn("Request " + requestClass + " cannot be associated with Arbiter " + c + ", already registered with " + previous);
                } else {
                    Class<? extends Serializable> resultClass = ClassUtils.getTypeArgument(c, Arbiter.class, 1);
                    resultMap.put(requestClass, resultClass);
                }
            }
        });
    }

    @Override
    public Class<? extends Serializable> getResultClass(Class<? extends RuleRequest> requestClass) {
        return resultMap.get(requestClass);
    }

    @Override
    public <R extends RuleRequest, A extends Arbiter<R, ? extends Serializable>> A getArbiter(RuleContext context) {
        Class<? extends RuleRequest> requestClass = context.getRequest().getClass();
        @SuppressWarnings("unchecked")
        Class<A> arbiterClass = (Class<A>)arbiterMap.get(requestClass);
        if (arbiterClass != null) {
            return applicationContext.getBean(arbiterClass, context);
        } else {
            throw new RuleException("No arbiter registered for request " + requestClass.getName());
        }
    }

    @Override
    public Collection<Class<? extends RuleRequest>> getKnownRequests() {
        return arbiterMap.keySet();
    }
}
