package com.example.cake.quiz.dto;

import com.example.cake.quiz.model.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for quiz result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    private String attemptId;
    private Integer attemptNumber;

    private Integer score;
    private Integer totalScore;
    private Double percentage;
    private Boolean passed;

    private String message;
    private Integer remainingAttempts; // null if unlimited

    private List<QuestionResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private String questionId;
        private String question;
        private List<String> selectedOptions;
        private List<String> correctAnswers;
        private Boolean isCorrect;
        private Integer pointsEarned;
        private Integer totalPoints;
        private String explanation;
    }

    /**
     * Create from QuizAttempt
     */
    public static QuizResultResponse from(QuizAttempt attempt, List<QuestionResult> results, Integer remainingAttempts) {
        String message;
        if (attempt.getPassed()) {
            message = String.format("🎉 Chúc mừng! Bạn đã đạt %d%% và PASS quiz!", attempt.getPercentage().intValue());
        } else {
            message = String.format("❌ Chưa đạt! Bạn đạt %d%%. Cần ít nhất 70%% để pass.", attempt.getPercentage().intValue());
            if (remainingAttempts != null && remainingAttempts > 0) {
                message += String.format(" Bạn còn %d lượt làm lại.", remainingAttempts);
            } else if (remainingAttempts != null && remainingAttempts == 0) {
                message += " Bạn đã hết lượt làm lại.";
            }
        }

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .totalScore(attempt.getTotalScore())
                .percentage(attempt.getPercentage())
                .passed(attempt.getPassed())
                .message(message)
                .remainingAttempts(remainingAttempts)
                .results(results)
                .build();
    }
}

