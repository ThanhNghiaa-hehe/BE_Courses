package com.example.cake.course.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String categoryCode;        // mã danh mục (DEV, DESIGN...)
    private String title;               // tiêu đề khóa học
    private String description;         // mô tả khóa học
    private Double price;               // giá gốc khóa học
    private String thumbnailUrl;        // ảnh đại diện khóa học

    private Integer duration;           // thời lượng (giờ)
    private String level;               // BEGINNER / INTERMEDIATE / ADVANCED
    private Boolean isPublished;        // đã publish hay chưa

    // Thêm các field mới để phù hợp với DTO
    private String instructorName;      // Tên giảng viên
    private Double rating;              // Đánh giá trung bình (0-5)
    private Integer totalStudents;      // Tổng số học viên đã đăng ký
    private Integer discountPercent;    // % giảm giá (0-100)
    private Double discountedPrice;     // Giá sau giảm (tính sẵn hoặc computed)
}
