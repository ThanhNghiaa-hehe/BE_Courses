package com.example.cake.lesson.controller;

import com.example.cake.lesson.dto.ChapterRequest;
import com.example.cake.lesson.model.Chapter;
import com.example.cake.lesson.service.ChapterService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing chapters
 */
@RestController
@RequestMapping("/api/admin/chapters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage<Chapter>> createChapter(@RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.createChapter(request));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseMessage<List<Chapter>>> getChaptersByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Chapter>> getChapterById(@PathVariable String id) {
        return ResponseEntity.ok(chapterService.getChapterById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseMessage<Chapter>> updateChapter(
            @PathVariable String id,
            @RequestBody ChapterRequest request
    ) {
        return ResponseEntity.ok(chapterService.updateChapter(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<String>> deleteChapter(@PathVariable String id) {
        return ResponseEntity.ok(chapterService.deleteChapter(id));
    }
}

