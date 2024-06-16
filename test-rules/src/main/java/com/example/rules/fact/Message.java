package com.example.rules.fact;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

    private final String text;

    public Message(String text) {
        this.text = text;
    }
}
