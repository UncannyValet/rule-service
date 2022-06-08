package com.example.rules.core.context;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class RuleContextFactoryImpl implements RuleContextFactory, ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @Override
    public RuleContext newContext(RuleRequest request, long runId) {
        return applicationContext.getBean(RuleContext.class, runId, request);
    }
}
