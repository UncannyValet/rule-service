package com.example.rules.core.domain;

import com.example.rules.api.RuleRequest;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rule_log")
public class RuleLog {

    @Id
    @GeneratedValue
    private Long id;

    @Column(updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime updateTime = LocalDateTime.now();

    @Column
    private RuleRequest.State state;

    @Column(updatable = false)
    private String requestClass;

    @Column(updatable = false)
    private int requestHash;

    @Lob
    @Column(updatable = false)
    private byte[] requestData;

    private String requestDescription;
    private String resultClass;
    private String resultDescription;
    private String message;

}
