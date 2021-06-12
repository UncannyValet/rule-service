package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class ArbiterRegistry {

    private List<Arbiter<?, ?>> arbiters;
    private Map<Class<? extends RuleRequest>, Arbiter<?, ?>> arbiterMap;

    public ArbiterRegistry(List<Arbiter<?, ?>> arbiters) {
        this.arbiters = arbiters;
    }

    @PostConstruct
    public void mapArbiters() {
        // Extract request classes and put in the map
    }
}
