package com.example.cake.category.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CourseCategoryCreateRequest {

    @NotBlank(message = "Mã danh mục không được để trống")
    private String code;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    private Boolean isActive = true; // Mặc định bật danh mục
}
