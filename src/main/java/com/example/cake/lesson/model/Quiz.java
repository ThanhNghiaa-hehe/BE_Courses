package com.example.cake.lesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Quiz - Bài kiểm tra
 * Embedded trong Lesson
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    private Integer passingScore;           // Điểm tối thiểu để pass (%, vd: 70)
    private Integer timeLimit;              // Giới hạn thời gian (phút, null = không giới hạn)
    private Integer maxAttempts;            // Số lần làm tối đa (null = unlimited)

    private List<Question> questions;       // Danh sách câu hỏi

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String id;                  // ID câu hỏi
        private String question;            // Nội dung câu hỏi
        private QuestionType type;          // Loại câu hỏi
        private List<Option> options;       // Các đáp án
        private String explanation;         // Giải thích đáp án đúng
        private Integer points;             // Điểm của câu hỏi (default: 1)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String id;                  // ID đáp án (a, b, c, d)
        private String text;                // Nội dung đáp án
        private Boolean isCorrect;          // Đáp án đúng hay sai
    }

    public enum QuestionType {
        SINGLE_CHOICE,      // Chọn 1 đáp án
        MULTIPLE_CHOICE,    // Chọn nhiều đáp án
        TRUE_FALSE          // Đúng/Sai
    }
}

