package com.example.cake.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating/updating quiz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    private String lessonId;
    private String courseId;
    private String chapterId;

    private String title;
    private String description;

    private Integer passingScore;
    private Integer timeLimit;
    private Integer maxAttempts;

    private List<QuestionRequest> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        private String id;
        private String question;
        private String type; // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
        private List<OptionRequest> options;
        private Integer points;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        private String id;
        private String text;
        private Boolean isCorrect;
    }
}

