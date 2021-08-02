package com.example.rules.fact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestResult implements Serializable {

    private List<String> messages = new ArrayList<>();

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void add(String message) {
        messages.add(message);
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "messages=" + messages +
                '}';
    }
}
