package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ArbiterRegistryImpl implements ArbiterRegistry {

    private final Map<Class<? extends RuleRequest>, ArbiterFactory<?, ?, ?>> factoryMap = new HashMap<>();

    public ArbiterRegistryImpl(List<ArbiterFactory<?, ?, ?>> factories) {
        // Map request classes to factories that provide arbiters for them
        factories.forEach(f -> factoryMap.put(f.getRequestClass(), f));
    }

    @Override
    public Class<? extends RuleResult> getResultClass(RuleRequest request) {
        return getResultClass(request.getClass());
    }

    @Override
    public Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass) {
        ArbiterFactory<?, ?, ?> factory = factoryMap.get(requestClass);
        return factory != null ? factory.getResultClass() : null;
    }

    @Override
    public <R extends RuleRequest, A extends Arbiter<R, ? extends RuleResult>> A getArbiter(R request) {
        ArbiterFactory<?, ?, ?> factory = factoryMap.get(request.getClass());
        return factory != null ? (A)factory.newArbiter() : null;
    }
}
