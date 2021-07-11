package com.example.rules.core.repository;

import com.example.rules.core.domain.RuleLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Repository
@ConditionalOnProperty("rule.log.enabled")
public interface RuleLogRepository extends CrudRepository<RuleLog, Long> {

    Stream<RuleLog> findByRequestClassAndRequestHashOrderByCreateTimeDesc(String requestClass, int requestHash);

    void deleteByCreateTimeBefore(LocalDateTime createTime);
}
