package com.example.cake.quiz.service;

import com.example.cake.quiz.dto.QuizRequest;
import com.example.cake.quiz.dto.QuizResultResponse;
import com.example.cake.quiz.dto.QuizSubmitRequest;
import com.example.cake.quiz.model.Quiz;
import com.example.cake.quiz.model.QuizAttempt;
import com.example.cake.quiz.repository.QuizAttemptRepository;
import com.example.cake.quiz.repository.QuizRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;

    /**
     * Create quiz
     */
    public ResponseMessage<Quiz> createQuiz(QuizRequest request) {
        // Convert request to Quiz entity
        List<Quiz.Question> questions = request.getQuestions().stream()
                .map(q -> Quiz.Question.builder()
                        .id(q.getId())
                        .question(q.getQuestion())
                        .type(Quiz.QuestionType.valueOf(q.getType()))
                        .points(q.getPoints())
                        .explanation(q.getExplanation())
                        .options(q.getOptions().stream()
                                .map(o -> Quiz.Option.builder()
                                        .id(o.getId())
                                        .text(o.getText())
                                        .isCorrect(o.getIsCorrect())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        Quiz quiz = Quiz.builder()
                .lessonId(request.getLessonId())
                .courseId(request.getCourseId())
                .chapterId(request.getChapterId())
                .title(request.getTitle())
                .description(request.getDescription())
                .passingScore(request.getPassingScore())
                .timeLimit(request.getTimeLimit())
                .maxAttempts(request.getMaxAttempts())
                .questions(questions)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        quiz = quizRepository.save(quiz);
        log.info("Created quiz: {}", quiz.getId());

        return new ResponseMessage<>(true, "Quiz created successfully", quiz);
    }

    /**
     * Get quiz by ID (for admin - show all answers)
     */
    public ResponseMessage<Quiz> getQuizById(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElse(null);

        if (quiz == null) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        return new ResponseMessage<>(true, "Success", quiz);
    }

    /**
     * Get quiz for student (hide correct answers)
     */
    public ResponseMessage<Quiz> getQuizForStudent(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElse(null);

        if (quiz == null) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        // Hide correct answers
        quiz.getQuestions().forEach(question -> {
            question.getOptions().forEach(option -> {
                option.setIsCorrect(null); // Hide correct flag
            });
        });

        return new ResponseMessage<>(true, "Success", quiz);
    }

    /**
     * Submit quiz
     */
    public ResponseMessage<QuizResultResponse> submitQuiz(String userId, QuizSubmitRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElse(null);

        if (quiz == null) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        // Check max attempts
        Integer attemptCount = attemptRepository.countByUserIdAndQuizId(userId, quiz.getId());
        if (quiz.getMaxAttempts() != null && attemptCount >= quiz.getMaxAttempts()) {
            return new ResponseMessage<>(false, "Bạn đã hết lượt làm quiz này", null);
        }

        // Grade quiz
        GradingResult gradingResult = gradeQuiz(quiz, request.getAnswers());

        // Calculate remaining attempts
        Integer remainingAttempts = null;
        if (quiz.getMaxAttempts() != null) {
            remainingAttempts = quiz.getMaxAttempts() - (attemptCount + 1);
        }

        // Save attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .userId(userId)
                .quizId(quiz.getId())
                .lessonId(quiz.getLessonId())
                .courseId(quiz.getCourseId())
                .attemptNumber(attemptCount + 1)
                .score(gradingResult.score)
                .totalScore(gradingResult.totalScore)
                .percentage(gradingResult.percentage)
                .passed(gradingResult.passed)
                .answers(gradingResult.answers)
                .timeSpent(request.getTimeSpent())
                .startedAt(request.getStartedAt())
                .completedAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);
        log.info("Saved quiz attempt: user={}, quiz={}, score={}/{}", 
                userId, quiz.getId(), attempt.getScore(), attempt.getTotalScore());

        // Create response
        QuizResultResponse response = QuizResultResponse.from(
                attempt, 
                gradingResult.questionResults, 
                remainingAttempts
        );

        return new ResponseMessage<>(true, "Quiz submitted successfully", response);
    }

    /**
     * Get quiz attempts history
     */
    public ResponseMessage<List<QuizAttempt>> getAttempts(String userId, String quizId) {
        List<QuizAttempt> attempts = attemptRepository
                .findByUserIdAndQuizIdOrderByAttemptNumberDesc(userId, quizId);

        return new ResponseMessage<>(true, "Success", attempts);
    }

    /**
     * Check if user has passed quiz
     */
    public ResponseMessage<Boolean> hasPassedQuiz(String userId, String quizId) {
        boolean passed = attemptRepository.existsByUserIdAndQuizIdAndPassedTrue(userId, quizId);
        return new ResponseMessage<>(true, "Success", passed);
    }

    /**
     * Update quiz
     */
    public ResponseMessage<Quiz> updateQuiz(String quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElse(null);

        if (quiz == null) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        // Update fields
        if (request.getTitle() != null) quiz.setTitle(request.getTitle());
        if (request.getDescription() != null) quiz.setDescription(request.getDescription());
        if (request.getPassingScore() != null) quiz.setPassingScore(request.getPassingScore());
        if (request.getTimeLimit() != null) quiz.setTimeLimit(request.getTimeLimit());
        if (request.getMaxAttempts() != null) quiz.setMaxAttempts(request.getMaxAttempts());

        if (request.getQuestions() != null) {
            List<Quiz.Question> questions = request.getQuestions().stream()
                    .map(q -> Quiz.Question.builder()
                            .id(q.getId())
                            .question(q.getQuestion())
                            .type(Quiz.QuestionType.valueOf(q.getType()))
                            .points(q.getPoints())
                            .explanation(q.getExplanation())
                            .options(q.getOptions().stream()
                                    .map(o -> Quiz.Option.builder()
                                            .id(o.getId())
                                            .text(o.getText())
                                            .isCorrect(o.getIsCorrect())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());
            quiz.setQuestions(questions);
        }

        quiz.setUpdatedAt(LocalDateTime.now());
        quiz = quizRepository.save(quiz);

        return new ResponseMessage<>(true, "Quiz updated successfully", quiz);
    }

    /**
     * Delete quiz
     */
    public ResponseMessage<Void> deleteQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        // Delete all attempts
        attemptRepository.deleteByQuizId(quizId);

        // Delete quiz
        quizRepository.deleteById(quizId);
        log.info("Deleted quiz: {}", quizId);

        return new ResponseMessage<>(true, "Quiz deleted successfully", null);
    }

    // ========== HELPER METHODS ==========

    /**
     * Grade quiz
     */
    private GradingResult gradeQuiz(Quiz quiz, List<QuizSubmitRequest.AnswerRequest> userAnswers) {
        int totalScore = 0;
        int earnedScore = 0;
        List<QuizAttempt.Answer> answers = new ArrayList<>();
        List<QuizResultResponse.QuestionResult> questionResults = new ArrayList<>();

        for (Quiz.Question question : quiz.getQuestions()) {
            totalScore += question.getPoints();

            // Find user's answer
            QuizSubmitRequest.AnswerRequest userAnswer = userAnswers.stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            List<String> selectedOptions = userAnswer != null ? userAnswer.getSelectedOptions() : new ArrayList<>();

            // Get correct answers
            List<String> correctAnswers = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(Quiz.Option::getId)
                    .collect(Collectors.toList());

            // Check if correct
            boolean isCorrect = selectedOptions.size() == correctAnswers.size() &&
                    selectedOptions.containsAll(correctAnswers) &&
                    correctAnswers.containsAll(selectedOptions);

            int pointsEarned = isCorrect ? question.getPoints() : 0;
            earnedScore += pointsEarned;

            // Save answer
            answers.add(QuizAttempt.Answer.builder()
                    .questionId(question.getId())
                    .selectedOptions(selectedOptions)
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .build());

            // Create question result
            questionResults.add(QuizResultResponse.QuestionResult.builder()
                    .questionId(question.getId())
                    .question(question.getQuestion())
                    .selectedOptions(selectedOptions)
                    .correctAnswers(correctAnswers)
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .totalPoints(question.getPoints())
                    .explanation(question.getExplanation())
                    .build());
        }

        double percentage = totalScore > 0 ? ((double) earnedScore / totalScore) * 100 : 0;
        boolean passed = percentage >= quiz.getPassingScore();

        return new GradingResult(earnedScore, totalScore, percentage, passed, answers, questionResults);
    }

    private static class GradingResult {
        int score;
        int totalScore;
        double percentage;
        boolean passed;
        List<QuizAttempt.Answer> answers;
        List<QuizResultResponse.QuestionResult> questionResults;

        GradingResult(int score, int totalScore, double percentage, boolean passed,
                      List<QuizAttempt.Answer> answers, List<QuizResultResponse.QuestionResult> questionResults) {
            this.score = score;
            this.totalScore = totalScore;
            this.percentage = percentage;
            this.passed = passed;
            this.answers = answers;
            this.questionResults = questionResults;
        }
    }
}

