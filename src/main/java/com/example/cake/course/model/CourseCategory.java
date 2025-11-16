package com.example.cake.course.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "course_categories")
public class CourseCategory {

    @Id
    private String id;

    private String code;        // DEV, DESIGN, MARKETING
    private String name;        // Lập trình, Thiết kế
    private String description; // mô tả danh mục
}
