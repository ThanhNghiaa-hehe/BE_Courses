package com.example.cake.lesson.dto;

import lombok.Data;

@Data
public class ChapterRequest {
    private String courseId;
    private String title;
    private String description;
    private Integer order;
    private Boolean isFree;
}

