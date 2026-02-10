package com.demo.nutgrow.dto;

import lombok.Data;

import java.util.List;


@Data
public class AnalysisResult {

    private String summary;
    private List<Part> parts;

    @Data
    public static class Part {
        private String title;
        private String content;
    }
}

