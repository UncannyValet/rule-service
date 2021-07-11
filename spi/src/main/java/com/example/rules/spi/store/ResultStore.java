package com.example.rules.spi.store;

import com.example.rules.api.RuleResult;

public interface ResultStore {

    <T extends RuleResult> void upload(long ruleId, T result);

    <T extends RuleResult> T download(long ruleId);
}
