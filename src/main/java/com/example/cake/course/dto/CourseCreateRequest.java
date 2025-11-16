package com.example.cake.course.dto;

import lombok.Data;

@Data
public class CourseCreateRequest {

    private String categoryCode;
    private String title;
    private String description;
    private Double price;
    private String thumbnailUrl;

    private Integer duration;       // thời lượng (giờ)
    private String level;           // Beginner/Intermediate/Advanced
    private Boolean isPublished;    // có công khai hay không
}
