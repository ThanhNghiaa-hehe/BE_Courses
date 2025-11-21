package com.example.cake.lesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lesson - Bài học
 * Mỗi Chapter có nhiều Lesson
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lessons")
public class Lesson {

    @Id
    private String id;

    private String chapterId;               // ID chương học
    private String courseId;                // ID khóa học (để query nhanh)

    // ========== BASIC INFO ==========
    private String title;                   // Tiêu đề bài học
    private String description;             // Mô tả ngắn
    private Integer order;                  // Thứ tự trong chương (1, 2, 3...)
    private Integer duration;               // Thời lượng (phút)

    // ========== ACCESS CONTROL ==========
    private Boolean isFree;                 // Miễn phí hay trả phí
    private String requiredPreviousLesson;  // ID bài học trước (null = không yêu cầu)

    // ========== VIDEO CONTENT ==========
    private String videoUrl;                // URL video (YouTube/Vimeo/etc)
    private String videoId;                 // ID video (extract từ URL)
    private VideoType videoType;            // Loại video
    private String videoThumbnail;          // Ảnh thumbnail video

    // ========== LEARNING CONTENT ==========
    private ContentType contentType;        // markdown, html
    private String content;                 // Nội dung bài học (Markdown/HTML)
    private String contentHtml;             // HTML rendered (nếu contentType = markdown)

    // ========== CODE SNIPPETS ==========
    private List<CodeSnippet> codeSnippets; // Code mẫu

    // ========== ATTACHMENTS ==========
    private List<Attachment> attachments;   // Tài liệu đính kèm

    // ========== QUIZ (OPTIONAL) ==========
    private Boolean hasQuiz;                // Có quiz hay không
    private Quiz quiz;                      // Quiz (nếu có)

    // ========== STATS ==========
    private Integer views;                  // Lượt xem
    private Integer likes;                  // Lượt thích

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== NESTED CLASSES ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeSnippet {
        private String language;            // java, javascript, html, css, python, etc
        private String code;                // Code content
        private String title;               // Tiêu đề (optional)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String name;                // Tên file
        private String url;                 // URL download
        private String type;                // pdf, zip, docx, etc
        private Long size;                  // Kích thước (bytes)
    }

    public enum VideoType {
        YOUTUBE,
        VIMEO,
        HOSTED           // Self-hosted
    }

    public enum ContentType {
        MARKDOWN,
        HTML
    }
}

