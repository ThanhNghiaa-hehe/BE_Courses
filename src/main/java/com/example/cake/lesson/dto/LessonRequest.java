package com.example.cake.lesson.dto;

import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.model.Quiz;
import lombok.Data;

import java.util.List;

@Data
public class LessonRequest {
    private String chapterId;
    private String courseId;

    private String title;
    private String description;
    private Integer order;
    private Integer duration;

    private Boolean isFree;
    private String requiredPreviousLesson;

    // Video
    private String videoUrl;
    private String videoId;
    private Lesson.VideoType videoType;
    private String videoThumbnail;

    // Content
    private Lesson.ContentType contentType;
    private String content;

    // Code snippets
    private List<Lesson.CodeSnippet> codeSnippets;

    // Attachments
    private List<Lesson.Attachment> attachments;

    // Quiz
    private Boolean hasQuiz;
    private Quiz quiz;
}

