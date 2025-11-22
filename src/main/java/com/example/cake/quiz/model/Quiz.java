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
 * Quiz entity - Independent from Lesson
 */
@Document(collection = "quizzes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    private String id;

    private String lessonId;        // Quiz belongs to which lesson
    private String courseId;
    private String chapterId;

    private String title;
    private String description;

    private Integer passingScore;   // Minimum score to pass (e.g., 70)
    private Integer timeLimit;      // Time limit in minutes
    private Integer maxAttempts;    // Max number of attempts (null = unlimited)

    private List<Question> questions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== NESTED CLASSES ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String id;
        private String question;
        private QuestionType type;
        private List<Option> options;
        private Integer points;
        private String explanation;  // Explanation when answer is wrong
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String id;
        private String text;
        private Boolean isCorrect;  // Correct answer flag
    }

    public enum QuestionType {
        SINGLE_CHOICE,      // Radio buttons
        MULTIPLE_CHOICE,    // Checkboxes
        TRUE_FALSE          // True/False question
    }
}

