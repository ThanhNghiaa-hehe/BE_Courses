package com.example.cake.lesson.dto;

import com.example.cake.lesson.model.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Chapter kèm progress của user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressDTO {

    // Thông tin chapter
    private String chapterId;
    private String title;
    private String description;
    private Integer order;
    private Integer totalLessons;
    private Integer totalDuration;

    // Progress
    private Boolean isUnlocked;             // Chapter đã unlock chưa
    private Integer completedLessons;       // Số lessons đã hoàn thành trong chapter
    private Integer progressPercent;        // % hoàn thành chapter

    // Quiz cuối chapter
    private String finalQuizId;             // ID quiz cuối chapter
    private Boolean hasFinalQuiz;           // Có quiz cuối không
    private Boolean quizPassed;             // Quiz đã pass chưa
    private Integer quizScore;              // Điểm quiz (nếu đã làm)

    /**
     * Tạo từ Chapter
     */
    public static ChapterProgressDTO from(Chapter chapter,
                                         Boolean isUnlocked,
                                         Integer completedLessons,
                                         String finalQuizId,
                                         Boolean quizPassed,
                                         Integer quizScore) {
        int progressPercent = 0;
        if (chapter.getTotalLessons() != null && chapter.getTotalLessons() > 0) {
            progressPercent = (completedLessons * 100) / chapter.getTotalLessons();
        }

        return ChapterProgressDTO.builder()
                .chapterId(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .order(chapter.getOrder())
                .totalLessons(chapter.getTotalLessons())
                .totalDuration(chapter.getTotalDuration())
                .isUnlocked(isUnlocked)
                .completedLessons(completedLessons)
                .progressPercent(progressPercent)
                .finalQuizId(finalQuizId)
                .hasFinalQuiz(finalQuizId != null)
                .quizPassed(quizPassed)
                .quizScore(quizScore)
                .build();
    }
}

