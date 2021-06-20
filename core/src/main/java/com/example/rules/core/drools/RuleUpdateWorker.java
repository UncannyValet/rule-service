package com.example.rules.core.drools;

import org.kie.api.builder.KieScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleUpdateWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RuleUpdateWorker.class);

    private final KieScanner scanner;

    public RuleUpdateWorker(KieScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void run() {
        try {
            scanner.scanNow();
        } catch (RuntimeException e) {
            LOG.error("Failed to scan for rule updates", e);
        }
    }
}
