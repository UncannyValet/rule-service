package com.example.rules.core.repository;

import com.example.rules.core.model.RuleLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleLogRepository extends CrudRepository<RuleLog, Long> {
}
