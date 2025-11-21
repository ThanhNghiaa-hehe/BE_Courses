package com.example.cake.lesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserProgress - Tiến độ học tập của user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_progress")
public class UserProgress {

    @Id
    private String id;

    private String userId;                      // ID user
    private String courseId;                    // ID khóa học

    // ========== OVERALL PROGRESS ==========
    private List<String> completedLessons;      // Danh sách lesson ID đã hoàn thành
    private String currentLessonId;             // Lesson đang học
    private Integer totalProgress;              // % tiến độ tổng (0-100)

    // ========== DETAILED PROGRESS ==========
    private List<LessonProgress> lessonsProgress; // Chi tiết từng bài học

    // ========== TIMESTAMPS ==========
    private LocalDateTime enrolledAt;           // Ngày đăng ký khóa học
    private LocalDateTime lastAccessedAt;       // Lần truy cập cuối
    private LocalDateTime completedAt;          // Ngày hoàn thành khóa học (null = chưa xong)

    // ========== NESTED CLASS ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonProgress {
        private String lessonId;                // ID bài học
        private Boolean completed;              // Đã hoàn thành chưa
        private LocalDateTime completedAt;      // Thời điểm hoàn thành

        private Integer timeSpent;              // Thời gian học (giây)
        private Integer videoProgress;          // % xem video (0-100)

        // Quiz progress
        private Integer quizScore;              // Điểm quiz (%, null = chưa làm)
        private Integer quizAttempts;           // Số lần làm quiz
        private LocalDateTime quizPassedAt;     // Thời điểm pass quiz
    }

    // ========== HELPER METHODS ==========

    public void markLessonComplete(String lessonId) {
        if (this.completedLessons == null) {
            this.completedLessons = new ArrayList<>();
        }
        if (!this.completedLessons.contains(lessonId)) {
            this.completedLessons.add(lessonId);
        }
    }

    public boolean isLessonCompleted(String lessonId) {
        return this.completedLessons != null && this.completedLessons.contains(lessonId);
    }
}

