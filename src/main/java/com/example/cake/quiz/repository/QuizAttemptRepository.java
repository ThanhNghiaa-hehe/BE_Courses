package com.example.cake.quiz.repository;

import com.example.cake.quiz.model.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    /**
     * Find all attempts by user and quiz
     */
    List<QuizAttempt> findByUserIdAndQuizIdOrderByAttemptNumberDesc(String userId, String quizId);

    /**
     * Find latest attempt
     */
    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByAttemptNumberDesc(String userId, String quizId);

    /**
     * Count attempts for a quiz by user
     */
    Integer countByUserIdAndQuizId(String userId, String quizId);

    /**
     * Find all attempts by user in a course
     */
    List<QuizAttempt> findByUserIdAndCourseId(String userId, String courseId);

    /**
     * Find passed attempts
     */
    List<QuizAttempt> findByUserIdAndQuizIdAndPassedTrue(String userId, String quizId);

    /**
     * Check if user has passed quiz
     */
    boolean existsByUserIdAndQuizIdAndPassedTrue(String userId, String quizId);

    /**
     * Delete attempts by quiz
     */
    void deleteByQuizId(String quizId);

    /**
     * Delete attempts by user
     */
    void deleteByUserId(String userId);
}

