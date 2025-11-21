package com.example.cake.lesson.controller;

import com.example.cake.lesson.dto.QuizResult;
import com.example.cake.lesson.dto.QuizSubmission;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.service.LessonService;
import com.example.cake.lesson.service.ProgressService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User controller for accessing lessons and tracking progress
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonUserController {

    private final LessonService lessonService;
    private final ProgressService progressService;

    /**
     * Lấy thông tin lesson (có kiểm tra quyền truy cập)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Lesson>> getLesson(
            @PathVariable String id,
            Authentication authentication
    ) {
        // Kiểm tra quyền truy cập
        String userEmail = authentication.getName();
        // TODO: Get userId from email
        // ResponseMessage<Boolean> access = progressService.canAccessLesson(userId, id);
        // if (!Boolean.TRUE.equals(access.getData())) {
        //     return ResponseEntity.status(403).body(new ResponseMessage<>(false, access.getMessage(), null));
        // }

        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    /**
     * Like lesson
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ResponseMessage<Lesson>> likeLesson(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.likeLesson(id));
    }

    /**
     * Đánh dấu lesson hoàn thành
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ResponseMessage<UserProgress>> markLessonComplete(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        // TODO: Get userId from email
        String userId = "temp-user-id"; // Temporary, cần implement getUserIdFromEmail

        return ResponseEntity.ok(progressService.markLessonComplete(userId, id));
    }

    /**
     * Cập nhật tiến độ video
     */
    @PostMapping("/{id}/progress")
    public ResponseEntity<ResponseMessage<UserProgress>> updateVideoProgress(
            @PathVariable String id,
            @RequestParam Integer percent,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        String userId = "temp-user-id";

        return ResponseEntity.ok(progressService.updateVideoProgress(userId, id, percent));
    }

    /**
     * Nộp bài quiz
     */
    @PostMapping("/quiz/submit")
    public ResponseEntity<ResponseMessage<QuizResult>> submitQuiz(
            @RequestBody QuizSubmission submission,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        String userId = "temp-user-id";

        return ResponseEntity.ok(progressService.submitQuiz(userId, submission));
    }

    /**
     * Kiểm tra quyền truy cập lesson
     */
    @GetMapping("/{id}/access")
    public ResponseEntity<ResponseMessage<Boolean>> checkAccess(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        String userId = "temp-user-id";

        return ResponseEntity.ok(progressService.canAccessLesson(userId, id));
    }
}

