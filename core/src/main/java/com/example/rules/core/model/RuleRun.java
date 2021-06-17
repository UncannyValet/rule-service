package com.example.rules.core.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RuleRun {

    @Id
    private long id;

    private String requestClass;

    private String resultClass;

}
