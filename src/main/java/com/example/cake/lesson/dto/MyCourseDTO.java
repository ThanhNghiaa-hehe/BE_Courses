package com.example.cake.lesson.dto;

import com.example.cake.course.model.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho "My Courses" - Khóa học user đã đăng ký kèm progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseDTO {

    // Thông tin khóa học
    private String courseId;
    private String title;
    private String thumbnailUrl;
    private String instructorName;
    private String level;

    // Tiến độ
    private Integer totalProgress;          // % hoàn thành (0-100)
    private Integer completedLessons;       // Số lessons đã hoàn thành
    private Integer totalLessons;           // Tổng số lessons
    private String currentLessonId;         // Lesson đang học
    private String currentLessonTitle;      // Tiêu đề lesson đang học

    // Thời gian
    private LocalDateTime enrolledAt;       // Ngày đăng ký
    private LocalDateTime lastAccessedAt;   // Lần học cuối
    private LocalDateTime completedAt;      // Ngày hoàn thành (null nếu chưa xong)

    // Trạng thái
    private Boolean isCompleted;            // Đã hoàn thành khóa học chưa

    /**
     * Tạo từ Course và UserProgress
     */
    public static MyCourseDTO from(Course course,
                                   com.example.cake.lesson.model.UserProgress progress,
                                   Long totalLessons,
                                   String currentLessonTitle) {
        return MyCourseDTO.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .instructorName(course.getInstructorName())
                .level(course.getLevel())
                .totalProgress(progress.getTotalProgress())
                .completedLessons(progress.getCompletedLessons() != null ? progress.getCompletedLessons().size() : 0)
                .totalLessons(totalLessons.intValue())
                .currentLessonId(progress.getCurrentLessonId())
                .currentLessonTitle(currentLessonTitle)
                .enrolledAt(progress.getEnrolledAt())
                .lastAccessedAt(progress.getLastAccessedAt())
                .completedAt(progress.getCompletedAt())
                .isCompleted(progress.getCompletedAt() != null)
                .build();
    }
}

