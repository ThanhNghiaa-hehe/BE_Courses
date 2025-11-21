package com.example.cake.lesson.controller;

import com.example.cake.lesson.dto.LessonRequest;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.service.LessonService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing lessons
 */
@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LessonAdminController {

    private final LessonService lessonService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage<Lesson>> createLesson(@RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.createLesson(request));
    }

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<ResponseMessage<List<Lesson>>> getLessonsByChapter(@PathVariable String chapterId) {
        return ResponseEntity.ok(lessonService.getLessonsByChapter(chapterId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseMessage<List<Lesson>>> getLessonsByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Lesson>> getLessonById(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseMessage<Lesson>> updateLesson(
            @PathVariable String id,
            @RequestBody LessonRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<String>> deleteLesson(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.deleteLesson(id));
    }
}

