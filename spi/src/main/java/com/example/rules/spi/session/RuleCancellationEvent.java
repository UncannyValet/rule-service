package com.example.rules.spi.session;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class RuleCancellationEvent extends ApplicationEvent {

    @Getter private final long sessionId;

    public RuleCancellationEvent(Object source, long sessionId) {
        super(source);

        this.sessionId = sessionId;
    }
}
