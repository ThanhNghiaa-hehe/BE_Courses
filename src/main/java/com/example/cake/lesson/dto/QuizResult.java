package com.example.cake.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    private Integer score;                  // Điểm đạt được (%)
    private Integer totalQuestions;         // Tổng số câu hỏi
    private Integer correctAnswers;         // Số câu trả lời đúng
    private Boolean passed;                 // Pass hay không
    private List<QuestionResult> results;   // Chi tiết từng câu hỏi

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private String questionId;
        private Boolean correct;
        private List<String> userAnswers;
        private List<String> correctAnswers;
        private String explanation;
    }
}

