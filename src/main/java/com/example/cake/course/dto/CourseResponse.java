package com.example.cake.course.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private String id;
    private String categoryCode;
    private String title;
    private String description;
    private Double price;
    private String thumbnailUrl;

    private Integer duration;
    private String level;
    private Boolean isPublished;

    // Thêm các trường để mapping với Course model và FavoriteItem
    private String instructorName;      // Tên giảng viên
    private Double rating;              // Đánh giá trung bình (0-5)
    private Integer totalStudents;      // Tổng số học viên đã đăng ký
    private Integer discountPercent;    // % giảm giá (0-100)
    private Double discountedPrice;     // Giá sau giảm
}
