package com.example.cake.course.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseCategoryDelete {
    @NotBlank(message = "Code is required")
    private String code ;
}
