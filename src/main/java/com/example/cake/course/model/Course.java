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

    private String categoryCode;   // mã danh mục (DEV, DESIGN...)
    private String title;          // tiêu đề khóa học
    private String description;    // mô tả khóa học
    private Double price;          // giá khóa học
    private String thumbnailUrl;   // ảnh đại diện khóa học

    private Integer duration;      // thời lượng (giờ)
    private String level;          // Beginner / Intermediate / Advanced
    private Boolean isPublished;   // đã publish hay chưa
}
