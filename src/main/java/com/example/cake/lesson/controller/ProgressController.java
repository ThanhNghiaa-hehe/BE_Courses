package com.example.cake.lesson.controller;

import com.example.cake.auth.model.User;
import com.example.cake.auth.repository.UserRepository;
import com.example.cake.lesson.dto.ChapterProgressDTO;
import com.example.cake.lesson.dto.MyCourseDTO;
import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.service.ProgressService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user progress tracking
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    /**
     * Helper method để lấy userId từ Authentication
     */
    private String getUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * Khởi tạo tiến độ khi user mua/đăng ký khóa học
     */
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<ResponseMessage<UserProgress>> enrollCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
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
        String userId = getUserId(authentication);
        return ResponseEntity.ok(progressService.getProgress(userId, courseId));
    }

    /**
     * MY COURSES - Lấy danh sách khóa học đã đăng ký
     */
    @GetMapping("/my-courses")
    public ResponseEntity<ResponseMessage<List<MyCourseDTO>>> getMyCourses(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(progressService.getMyCourses(userId));
    }

    /**
     * Lấy danh sách chapters kèm progress của user
     */
    @GetMapping("/course/{courseId}/chapters")
    public ResponseEntity<ResponseMessage<List<ChapterProgressDTO>>> getChaptersWithProgress(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(progressService.getChaptersWithProgress(userId, courseId));
    }
}

