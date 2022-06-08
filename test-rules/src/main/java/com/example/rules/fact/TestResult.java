package com.example.rules.fact;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TestResult implements Serializable {

    private List<String> messages = new ArrayList<>();

    public void add(String message) {
        messages.add(message);
    }

}
