package com.example.rules.core.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RuleLog {

    @Id
    private Long id;

    private String requestClass;

    private String resultClass;

}
