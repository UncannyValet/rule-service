package com.example.rules.spi.store;

import java.io.Serializable;

public interface ResultStore {

    <T extends Serializable> void save(long ruleId, T result);

    <T extends Serializable> T load(long ruleId);
}
