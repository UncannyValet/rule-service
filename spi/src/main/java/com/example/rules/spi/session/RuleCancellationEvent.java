package com.example.rules.spi.session;

import org.springframework.context.ApplicationEvent;

public class RuleCancellationEvent extends ApplicationEvent {

    private final long sessionId;

    public RuleCancellationEvent(Object source, long sessionId) {
        super(source);

        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return sessionId;
    }
}
