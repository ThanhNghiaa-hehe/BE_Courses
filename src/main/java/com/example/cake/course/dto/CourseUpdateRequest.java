package com.example.cake.course.dto;

import lombok.Data;

@Data
public class CourseUpdateRequest {

    private String id;              // ID khóa học
    private String categoryCode;
    private String title;
    private String description;
    private Double price;
    private String thumbnailUrl;

    private Integer duration;
    private String level;
    private Boolean isPublished;
}
