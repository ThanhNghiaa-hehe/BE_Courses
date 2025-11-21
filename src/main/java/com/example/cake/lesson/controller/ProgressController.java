package com.example.cake.lesson.controller;

import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.service.ProgressService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user progress tracking
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * Khởi tạo tiến độ khi user mua/đăng ký khóa học
     */
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<ResponseMessage<UserProgress>> enrollCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        String userId = "temp-user-id"; // TODO: Get from User service

        return ResponseEntity.ok(progressService.initializeProgress(userId, courseId));
    }

    /**
     * Lấy tiến độ của user trong một khóa học
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseMessage<UserProgress>> getProgress(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        String userId = "temp-user-id";

        return ResponseEntity.ok(progressService.getProgress(userId, courseId));
    }
}

