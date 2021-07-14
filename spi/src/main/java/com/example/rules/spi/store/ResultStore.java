package com.example.rules.spi.store;

import com.example.rules.api.RuleResult;

public interface ResultStore {

    <T extends RuleResult> void save(long ruleId, T result);

    <T extends RuleResult> T load(long ruleId);
}
