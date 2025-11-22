package com.example.cake.quiz.controller;

import com.example.cake.quiz.dto.QuizResultResponse;
import com.example.cake.quiz.dto.QuizSubmitRequest;
import com.example.cake.quiz.model.Quiz;
import com.example.cake.quiz.model.QuizAttempt;
import com.example.cake.quiz.service.QuizService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller for quiz taking
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizUserController {

    private final QuizService quizService;

    /**
     * Get quiz for student (without correct answers)
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ResponseMessage<Quiz>> getQuiz(
            @PathVariable String quizId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.getQuizForStudent(quizId));
    }

    /**
     * Submit quiz
     */
    @PostMapping("/submit")
    public ResponseEntity<ResponseMessage<QuizResultResponse>> submitQuiz(
            @RequestBody QuizSubmitRequest request,
            Authentication authentication
    ) {
        String userId = "temp-user-id"; // TODO: Get from authentication
        return ResponseEntity.ok(quizService.submitQuiz(userId, request));
    }

    /**
     * Get quiz attempts history
     */
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<ResponseMessage<List<QuizAttempt>>> getAttempts(
            @PathVariable String quizId,
            Authentication authentication
    ) {
        String userId = "temp-user-id";
        return ResponseEntity.ok(quizService.getAttempts(userId, quizId));
    }

    /**
     * Check if user has passed quiz
     */
    @GetMapping("/{quizId}/passed")
    public ResponseEntity<ResponseMessage<Boolean>> hasPassedQuiz(
            @PathVariable String quizId,
            Authentication authentication
    ) {
        String userId = "temp-user-id";
        return ResponseEntity.ok(quizService.hasPassedQuiz(userId, quizId));
    }
}

