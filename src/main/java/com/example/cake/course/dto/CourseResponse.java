package com.example.cake.course.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private String id;
    private String categoryCode;
    private String title;
    private String description;
    private Double price;
    private String thumbnailUrl;

    private Integer duration;
    private String level;
    private Boolean isPublished;
}
