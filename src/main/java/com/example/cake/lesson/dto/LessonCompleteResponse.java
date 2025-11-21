package com.example.cake.lesson.dto;

import com.example.cake.lesson.model.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response khi complete lesson - bao gồm lesson tiếp theo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompleteResponse {

    private Boolean completed;              // Đã complete thành công
    private Integer totalProgress;          // % tổng tiến độ khóa học
    private Integer completedLessons;       // Số lessons đã hoàn thành
    private Integer totalLessons;           // Tổng số lessons

    // Lesson tiếp theo (nếu có và đã unlock)
    private NextLesson nextLesson;

    // Thông báo
    private String message;
    private Boolean courseCompleted;        // Đã hoàn thành khóa học chưa

    // Hành động gợi ý khi không có next lesson
    private String suggestedAction;         // "RETAKE_QUIZ", "COMPLETE_REQUIRED", "COURSE_DONE", null
    private String requiredLessonId;        // ID lesson cần complete (nếu có)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextLesson {
        private String id;
        private String title;
        private String description;
        private Integer duration;
        private String chapterId;
        private String chapterTitle;
        private Integer order;
        private Boolean isFree;
        private Boolean hasQuiz;
        private Boolean isUnlocked;         // Đã unlock chưa (previous lesson complete)

        public static NextLesson fromLesson(Lesson lesson, String chapterTitle, Boolean isUnlocked) {
            if (lesson == null) return null;

            return NextLesson.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .description(lesson.getDescription())
                    .duration(lesson.getDuration())
                    .chapterId(lesson.getChapterId())
                    .chapterTitle(chapterTitle)
                    .order(lesson.getOrder())
                    .isFree(lesson.getIsFree())
                    .hasQuiz(lesson.getHasQuiz())
                    .isUnlocked(isUnlocked)
                    .build();
        }
    }
}

