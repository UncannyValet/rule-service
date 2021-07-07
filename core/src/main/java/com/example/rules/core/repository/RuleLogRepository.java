package com.example.rules.core.repository;

import com.example.rules.core.model.RuleLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Repository
public interface RuleLogRepository extends CrudRepository<RuleLog, Long> {

    Stream<RuleLog> findByRequestClassAndRequestHashOrderByCreateTimeDesc(String requestClass, int requestHash);

    void deleteByCreateTimeBefore(LocalDateTime createTime);
}
