package com.example.rules.spi.session;

import org.springframework.context.ApplicationEvent;

public class RuleCancellationEvent extends ApplicationEvent {

    private final String sessionId;

    public RuleCancellationEvent(Object source, String sessionId) {
        super(source);

        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
