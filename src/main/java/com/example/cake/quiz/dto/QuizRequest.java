package com.example.cake.quiz.dto;

import com.example.cake.quiz.model.Quiz;
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

    private Integer passingScore;  // Default: 70
    private Integer timeLimit;     // In seconds, default: 600 (10 minutes)
    private Integer maxAttempts;   // null = unlimited

    private List<QuestionRequest> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        private String id;              // Optional for update
        private String question;        // Question text
        private String type;            // "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE"
        private List<OptionRequest> options;
        private Integer points;         // Points for this question
        private String explanation;     // Explanation when wrong
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

