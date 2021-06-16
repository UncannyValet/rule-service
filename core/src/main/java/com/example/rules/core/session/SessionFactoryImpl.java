package com.example.rules.core.session;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.session.RuleSession;

public class SessionFactoryImpl implements SessionFactory {

    @Override
    public <R extends RuleRequest> RuleSession getSession(R request, String... ruleSets) {
        return null;
    }
}
