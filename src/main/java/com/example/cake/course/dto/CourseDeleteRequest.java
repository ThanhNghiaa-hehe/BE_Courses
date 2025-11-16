package com.example.cake.course.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class CourseCategoryDeleteRequest {
    @NotBlank
    private String code;
}
