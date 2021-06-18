package com.example.rules.core.drools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.builder.KieScanner;

public class RuleUpdateWorker implements Runnable {

    private static final Logger LOG = LogManager.getLogger(RuleUpdateWorker.class);

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
