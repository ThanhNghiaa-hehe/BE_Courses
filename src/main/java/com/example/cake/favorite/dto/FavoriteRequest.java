package com.example.cake.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteRequest {
    private String courseId;           // ID khóa học (lấy từ course.id)
    private String title;              // Tên khóa học (lấy từ course.title)
    private String thumbnailUrl;       // Ảnh thumbnail khóa học (lấy từ course.thumbnailUrl)
    private Double price;              // Giá gốc (lấy từ course.price)
    private Double discountedPrice;    // Giá sau giảm (lấy từ course.discountedPrice)
    private Integer discountPercent;   // % giảm giá (lấy từ course.discountPercent)

    // Các trường cho khóa học - KHỚP VỚI COURSE MODEL
    private String level;              // BEGINNER, INTERMEDIATE, ADVANCED (lấy từ course.level)
    private Integer duration;          // Thời lượng (giờ) (lấy từ course.duration)
    private String instructorName;     // Tên giảng viên (lấy từ course.instructorName)
    private Double rating;             // Đánh giá trung bình (lấy từ course.rating)
    private Integer totalStudents;     // Số học viên (lấy từ course.totalStudents)

    // BỎ quantity - khóa học không có số lượng
}

