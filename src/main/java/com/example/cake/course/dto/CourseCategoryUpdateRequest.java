package com.example.cake.course.dto;

import lombok.Data;

@Data
public class CourseCategoryUpdateRequest {
    private String id;          // MongoDB ID
    private String code;
    private String name;
    private String description;
}
