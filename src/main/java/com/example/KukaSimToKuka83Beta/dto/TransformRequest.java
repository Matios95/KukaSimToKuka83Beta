package com.example.KukaSimToKuka83Beta.dto;

import jakarta.validation.constraints.NotBlank;

public class TransformRequest {


    @NotBlank(message = "Tekst nie może być pusty")
    private String text;


    public TransformRequest() {}


    public TransformRequest(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }
}