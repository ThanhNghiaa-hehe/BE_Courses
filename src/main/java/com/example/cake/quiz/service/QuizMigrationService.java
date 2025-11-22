package com.example.cake.quiz.service;

import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.repository.LessonRepository;
import com.example.cake.quiz.model.Quiz;
import com.example.cake.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to migrate old embedded quizzes to new Quiz entity
 * Run this once to migrate existing data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizMigrationService {

    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;

    /**
     * Migrate all embedded quizzes from lessons to Quiz collection
     * WARNING: This should be run ONCE during deployment
     */
    public String migrateQuizzesFromLessons() {
        log.info("Starting quiz migration...");

        // Find all lessons with hasQuiz = true
        List<Lesson> allLessons = lessonRepository.findAll();
        List<Lesson> lessonsWithQuiz = allLessons.stream()
                .filter(lesson -> Boolean.TRUE.equals(lesson.getHasQuiz()))
                .collect(Collectors.toList());

        if (lessonsWithQuiz.isEmpty()) {
            log.info("No lessons with quiz found. Migration skipped.");
            return "No quizzes to migrate";
        }

        int migratedCount = 0;
        int skippedCount = 0;

        for (Lesson lesson : lessonsWithQuiz) {
            try {
                // Check if quiz already migrated (lesson has quizId)
                if (lesson.getQuizId() != null && !lesson.getQuizId().isEmpty()) {
                    log.info("Lesson {} already has quizId, skipping", lesson.getId());
                    skippedCount++;
                    continue;
                }

                // Check if quiz for this lesson already exists
                if (quizRepository.findByLessonId(lesson.getId()).isPresent()) {
                    log.info("Quiz for lesson {} already exists, skipping", lesson.getId());
                    skippedCount++;
                    continue;
                }

                // NOTE: Old embedded quiz data is no longer accessible
                // This migration assumes lessons don't have old embedded quiz data
                // New quizzes should be created via Admin API

                log.info("Lesson {} has hasQuiz=true but no quiz data. Skipping.", lesson.getId());
                skippedCount++;

            } catch (Exception e) {
                log.error("Error migrating quiz for lesson {}: {}", lesson.getId(), e.getMessage());
                skippedCount++;
            }
        }

        String result = String.format(
                "Quiz migration completed. Migrated: %d, Skipped: %d, Total: %d",
                migratedCount, skippedCount, lessonsWithQuiz.size()
        );

        log.info(result);
        return result;
    }

    /**
     * Create quiz from admin request (new approach)
     * This is the recommended way to create quizzes after migration
     */
    public String createQuizForLesson(String lessonId, Quiz quiz) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "Lesson not found";
        }

        // Create quiz
        quiz.setLessonId(lessonId);
        quiz.setCourseId(lesson.getCourseId());
        quiz.setChapterId(lesson.getChapterId());
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());

        Quiz savedQuiz = quizRepository.save(quiz);

        // Update lesson to reference quiz
        lesson.setHasQuiz(true);
        lesson.setQuizId(savedQuiz.getId());
        lessonRepository.save(lesson);

        log.info("Created quiz {} for lesson {}", savedQuiz.getId(), lessonId);
        return "Quiz created successfully";
    }
}

