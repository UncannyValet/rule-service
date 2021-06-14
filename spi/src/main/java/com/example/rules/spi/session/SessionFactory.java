package com.example.rules.spi.session;

import com.example.rules.api.RuleRequest;

public interface SessionFactory {

    <R extends RuleRequest> RuleSession getSession(R request, String... ruleSets);
}
