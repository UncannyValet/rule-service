package com.example.rules.spi.session;

import com.example.rules.api.RuleInfo;
import com.example.rules.api.RuleRequest;

import java.util.stream.Stream;

public interface SessionFactory {

    <R extends RuleRequest> RuleSession getSession(R request, String... ruleSets);

    void registerContainer(String groupId, String artifactId, String versionRange);

    void registerContainer(RuleContainer container);

    void deregisterContainer(String id);

    Stream<RuleInfo> getRuleInfo();

    Stream<RuleInfo> getRuleInfo(RuleRequest request);
}
