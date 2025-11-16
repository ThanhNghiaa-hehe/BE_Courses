package com.example.cake.course.dto;

import lombok.Data;

@Data
public class CourseCategoryCreateRequest {
    private String code;        // vd: DEV, DESIGN
    private String name;        // Lập trình, Thiết kế
    private String description; // mô tả danh mục
}
