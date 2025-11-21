package com.example.cake.lesson.controller;

import com.example.cake.lesson.model.Chapter;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.service.ChapterService;
import com.example.cake.lesson.service.LessonService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public/User controller for viewing course curriculum
 * Không cần authentication - để user xem trước khi mua
 */
@RestController
@RequestMapping("/api/curriculum")
@RequiredArgsConstructor
public class CurriculumController {

    private final ChapterService chapterService;
    private final LessonService lessonService;

    /**
     * Lấy tất cả chapters của một khóa học (PUBLIC)
     * User có thể xem để quyết định mua khóa học
     */
    @GetMapping("/course/{courseId}/chapters")
    public ResponseEntity<ResponseMessage<List<Chapter>>> getCourseChapters(@PathVariable String courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    /**
     * Lấy chi tiết một chapter (PUBLIC)
     */
    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ResponseMessage<Chapter>> getChapterById(@PathVariable String chapterId) {
        return ResponseEntity.ok(chapterService.getChapterById(chapterId));
    }

    /**
     * Lấy tất cả lessons của một chapter (PUBLIC)
     * User có thể xem curriculum trước khi mua
     * Note: Content chi tiết sẽ bị ẩn nếu chưa mua khóa học
     */
    @GetMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<ResponseMessage<List<Lesson>>> getChapterLessons(@PathVariable String chapterId) {
        return ResponseEntity.ok(lessonService.getLessonsByChapter(chapterId));
    }

    /**
     * Lấy toàn bộ curriculum của khóa học (PUBLIC)
     * Bao gồm chapters và lessons (overview)
     */
    @GetMapping("/course/{courseId}/full")
    public ResponseEntity<ResponseMessage<List<Lesson>>> getFullCurriculum(@PathVariable String courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }
}

