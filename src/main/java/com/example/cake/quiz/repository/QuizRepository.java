package com.example.cake.quiz.repository;

import com.example.cake.quiz.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    /**
     * Find quiz by lesson ID
     */
    Optional<Quiz> findByLessonId(String lessonId);

    /**
     * Find all quizzes in a course
     */
    List<Quiz> findByCourseId(String courseId);

    /**
     * Find all quizzes in a chapter
     */
    List<Quiz> findByChapterId(String chapterId);

    /**
     * Delete quiz by lesson ID
     */
    void deleteByLessonId(String lessonId);

    /**
     * Delete all quizzes in a course
     */
    void deleteByCourseId(String courseId);

    /**
     * Delete all quizzes in a chapter
     */
    void deleteByChapterId(String chapterId);
}

