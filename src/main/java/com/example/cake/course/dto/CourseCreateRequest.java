package com.example.cake.category.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.math.BigDecimal;
@Data
public class CourseCreateRequest {

    @NotBlank
    private String categoryCode;

    @NotBlank
    private String name;

    private String description;
    private BigDecimal price;
    private String imageUrl; // Thay cho images
    private Boolean isAvailable = true;

    private Integer duration; // thời lượng khóa học
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    private Double discountPercentage;
}
