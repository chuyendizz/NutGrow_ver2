package com.demo.nutgrow.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizResult {

    private List<Question> questions;

    @Data
    public static class Question {
        private String question;
        private List<String> options;
        private int answer;
    }
}
