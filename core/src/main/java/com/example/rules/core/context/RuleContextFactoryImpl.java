package com.example.rules.core.context;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RuleContextFactoryImpl implements RuleContextFactory, ApplicationContextAware {

    private final AtomicLong idGenerator = new AtomicLong();

    private ApplicationContext applicationContext;

    @Override
    public RuleContext newContext(RuleRequest request) {
        return applicationContext.getBean(RuleContext.class, idGenerator.getAndIncrement(), request);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
