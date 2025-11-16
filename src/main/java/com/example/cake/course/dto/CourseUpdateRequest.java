package com.example.cake.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;




@Data
public class CourseCategoryUpdateRequest {

    @NotBlank(message = "ID danh mục không được bỏ trống")
    private String id;

    private String name;
    private String description;
    private Boolean isActive;
}

