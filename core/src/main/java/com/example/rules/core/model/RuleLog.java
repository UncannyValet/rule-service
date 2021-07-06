package com.example.rules.core.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_log")
public class RuleLog {

    @Id
    @GeneratedValue
    private Long id;

    @Column(updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(updatable = false)
    private String state;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(String requestClass) {
        this.requestClass = requestClass;
    }

    public int getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(int requestHash) {
        this.requestHash = requestHash;
    }

    public byte[] getRequestData() {
        return requestData;
    }

    public void setRequestData(byte[] requestData) {
        this.requestData = requestData;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestSummary) {
        this.requestDescription = requestSummary;
    }

    public String getResultClass() {
        return resultClass;
    }

    public void setResultClass(String resultClass) {
        this.resultClass = resultClass;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultSummary) {
        this.resultDescription = resultSummary;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
