package com.example.cake.lesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CourseOverview - Thông tin tổng quan khóa học
 * Embedded trong Course model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOverview {

    // ========== MÔ TẢ TỔNG QUAN ==========
    private String description;                 // Mô tả chi tiết khóa học

    // ========== BẠN SẼ HỌC ĐƯỢC GÌ? ==========
    private List<String> whatYouWillLearn;      // Danh sách kỹ năng học được

    // ========== YÊU CẦU ĐẦU VÀO ==========
    private List<String> requirements;          // Yêu cầu để học khóa này

    // ========== DÀNH CHO AI? ==========
    private List<String> targetAudience;        // Đối tượng phù hợp

    // ========== VIDEO GIỚI THIỆU ==========
    private String introVideoUrl;               // URL video giới thiệu
    private String introVideoId;                // ID video
    private Lesson.VideoType introVideoType;    // Loại video (youtube/vimeo)

    // ========== THỐNG KÊ ==========
    private CourseStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStats {
        private Integer totalChapters;          // Tổng số chương
        private Integer totalLessons;           // Tổng số bài học
        private Integer totalDuration;          // Tổng thời lượng (phút)
        private Integer totalStudents;          // Tổng số học viên
        private Double averageRating;           // Đánh giá trung bình
        private Boolean certificateProvided;    // Có cấp chứng chỉ không
    }
}

