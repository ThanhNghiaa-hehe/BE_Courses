package com.example.cake.quiz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QuizAttempt - Track user's quiz submission history
 */
@Document(collection = "quiz_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    private String id;

    private String userId;
    private String quizId;
    private String lessonId;
    private String courseId;

    private Integer attemptNumber;      // 1, 2, 3... (which attempt)
    private Integer score;              // Points earned
    private Integer totalScore;         // Total possible points
    private Double percentage;          // Score percentage

    private Boolean passed;             // Pass or fail

    private List<Answer> answers;       // User's answers

    private Integer timeSpent;          // Time spent in seconds
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // ========== NESTED CLASS ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private String questionId;
        private List<String> selectedOptions;  // Selected option IDs
        private Boolean isCorrect;
        private Integer pointsEarned;
    }
}

