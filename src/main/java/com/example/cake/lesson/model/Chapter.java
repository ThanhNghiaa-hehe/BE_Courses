package com.example.cake.lesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Chapter - Chương học
 * Mỗi Course có nhiều Chapter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chapters")
public class Chapter {

    @Id
    private String id;

    private String courseId;                // ID khóa học

    private String title;                   // Tiêu đề chương (vd: "Làm quen HTML")
    private String description;             // Mô tả ngắn

    private Integer order;                  // Thứ tự chương (1, 2, 3...)

    private Integer totalLessons;           // Tổng số bài học trong chương
    private Integer totalDuration;          // Tổng thời lượng (phút)

    private Boolean isFree;                 // Chương miễn phí hay không

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

