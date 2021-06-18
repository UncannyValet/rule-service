package com.example.rules.core.arbiter;

import com.example.rules.api.RuleException;
import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.arbiter.Arbiter;
import com.example.rules.spi.utils.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atteo.classindex.ClassIndex;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.example.rules.api.ErrorNumbers.ARBITER_NOT_REGISTERED;

@Component
public class ArbiterFactoryImpl implements ArbiterFactory, ApplicationContextAware {

    private static final Logger LOG = LogManager.getLogger(ArbiterFactoryImpl.class);

    private ApplicationContext applicationContext;

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends RuleRequest>, Class<? extends Arbiter>> arbiterMap = new HashMap<>();
    private final Map<Class<? extends RuleRequest>, Class<? extends RuleResult>> resultMap = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public ArbiterFactoryImpl() {
        ClassIndex.getSubclasses(Arbiter.class).forEach(c -> {
            if (ClassUtils.canInstantiate(c)) {
                Class<? extends RuleRequest> requestClass = ClassUtils.getTypeArgument(c, Arbiter.class, 0);
                Class<? extends Arbiter> previous = arbiterMap.putIfAbsent(requestClass, c);
                if (previous != null) {
                    LOG.warn("Request " + requestClass + " cannot be associated with Arbiter " + c + ", already registered with " + previous);
                } else {
                    Class<? extends RuleResult> resultClass = ClassUtils.getTypeArgument(c, Arbiter.class, 1);
                    resultMap.put(requestClass, resultClass);
                }
            }
        });
    }

    @Override
    public Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass) {
        return resultMap.get(requestClass);
    }

    @Override
    public <R extends RuleRequest, A extends Arbiter<R, ? extends RuleResult>> A getArbiter(RuleContext context) {
        @SuppressWarnings("unchecked")
        Class<A> arbiterClass = (Class<A>)arbiterMap.get(context.getRequest().getClass());
        if (arbiterClass != null) {
            return applicationContext.getBean(arbiterClass, context);
        } else {
            throw new RuleException(ARBITER_NOT_REGISTERED);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
