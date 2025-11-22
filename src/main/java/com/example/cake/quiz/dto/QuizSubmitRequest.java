package com.example.cake.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for submitting quiz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {

    private String quizId;
    private List<AnswerRequest> answers;
    private Integer timeSpent; // Time spent in seconds
    private LocalDateTime startedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        private String questionId;
        private List<String> selectedOptions; // Option IDs
    }
}

