package com.example.cake.lesson.service;

import com.example.cake.lesson.dto.QuizResult;
import com.example.cake.lesson.dto.QuizSubmission;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.model.Quiz;
import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.repository.LessonRepository;
import com.example.cake.lesson.repository.UserProgressRepository;
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
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final LessonRepository lessonRepository;

    /**
     * Khởi tạo tiến độ khi user đăng ký khóa học
     */
    public ResponseMessage<UserProgress> initializeProgress(String userId, String courseId) {
        // Kiểm tra đã có progress chưa
        UserProgress existing = progressRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        if (existing != null) {
            return new ResponseMessage<>(true, "Progress already exists", existing);
        }

        UserProgress progress = UserProgress.builder()
                .userId(userId)
                .courseId(courseId)
                .completedLessons(new ArrayList<>())
                .currentLessonId(null)
                .totalProgress(0)
                .lessonsProgress(new ArrayList<>())
                .enrolledAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        progressRepository.save(progress);
        log.info("Initialized progress for user: {} in course: {}", userId, courseId);

        return new ResponseMessage<>(true, "Progress initialized", progress);
    }

    /**
     * Lấy tiến độ của user trong một khóa học
     */
    public ResponseMessage<UserProgress> getProgress(String userId, String courseId) {
        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);

        if (progress == null) {
            return new ResponseMessage<>(false, "Progress not found. User may not be enrolled in this course.", null);
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return new ResponseMessage<>(true, "Success", progress);
    }

    /**
     * Đánh dấu lesson đã hoàn thành
     */
    public ResponseMessage<UserProgress> markLessonComplete(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, lesson.getCourseId()).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "Progress not found", null);
        }

        // Thêm vào danh sách completed
        progress.markLessonComplete(lessonId);

        // Cập nhật lesson progress
        UserProgress.LessonProgress lessonProgress = findOrCreateLessonProgress(progress, lessonId);
        lessonProgress.setCompleted(true);
        lessonProgress.setCompletedAt(LocalDateTime.now());
        lessonProgress.setVideoProgress(100);

        // Cập nhật tổng tiến độ
        updateTotalProgress(progress, lesson.getCourseId());

        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);

        log.info("User {} completed lesson {}", userId, lessonId);
        return new ResponseMessage<>(true, "Lesson marked as complete", progress);
    }

    /**
     * Cập nhật tiến độ xem video
     */
    public ResponseMessage<UserProgress> updateVideoProgress(String userId, String lessonId, Integer percent) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, lesson.getCourseId()).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "Progress not found", null);
        }

        UserProgress.LessonProgress lessonProgress = findOrCreateLessonProgress(progress, lessonId);
        lessonProgress.setVideoProgress(percent);

        // Nếu xem đến 90% thì tự động đánh dấu complete
        if (percent >= 90 && !Boolean.TRUE.equals(lessonProgress.getCompleted())) {
            lessonProgress.setCompleted(true);
            lessonProgress.setCompletedAt(LocalDateTime.now());
            progress.markLessonComplete(lessonId);
            updateTotalProgress(progress, lesson.getCourseId());
        }

        progress.setCurrentLessonId(lessonId);
        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return new ResponseMessage<>(true, "Video progress updated", progress);
    }

    /**
     * Nộp bài quiz
     */
    public ResponseMessage<QuizResult> submitQuiz(String userId, QuizSubmission submission) {
        Lesson lesson = lessonRepository.findById(submission.getLessonId()).orElse(null);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getHasQuiz())) {
            return new ResponseMessage<>(false, "Quiz not found", null);
        }

        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, lesson.getCourseId()).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "Progress not found", null);
        }

        // Chấm điểm
        QuizResult result = gradeQuiz(lesson.getQuiz(), submission);

        // Cập nhật progress
        UserProgress.LessonProgress lessonProgress = findOrCreateLessonProgress(progress, submission.getLessonId());
        lessonProgress.setQuizScore(result.getScore());
        lessonProgress.setQuizAttempts((lessonProgress.getQuizAttempts() != null ? lessonProgress.getQuizAttempts() : 0) + 1);

        // Nếu pass quiz
        if (Boolean.TRUE.equals(result.getPassed())) {
            lessonProgress.setQuizPassedAt(LocalDateTime.now());
            lessonProgress.setCompleted(true);
            lessonProgress.setCompletedAt(LocalDateTime.now());
            progress.markLessonComplete(submission.getLessonId());
            updateTotalProgress(progress, lesson.getCourseId());
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);

        log.info("User {} submitted quiz for lesson {}, score: {}", userId, submission.getLessonId(), result.getScore());
        return new ResponseMessage<>(true, "Quiz submitted", result);
    }

    /**
     * Kiểm tra xem user có quyền truy cập lesson không
     */
    public ResponseMessage<Boolean> canAccessLesson(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        // Nếu lesson free → OK
        if (Boolean.TRUE.equals(lesson.getIsFree())) {
            return new ResponseMessage<>(true, "Access granted (free lesson)", true);
        }

        // Kiểm tra user đã enroll khóa học chưa
        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, lesson.getCourseId()).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "User not enrolled in this course", false);
        }

        // Kiểm tra lesson trước đã hoàn thành chưa (unlock tuần tự)
        if (lesson.getRequiredPreviousLesson() != null) {
            if (!progress.isLessonCompleted(lesson.getRequiredPreviousLesson())) {
                return new ResponseMessage<>(false, "Previous lesson not completed", false);
            }
        }

        return new ResponseMessage<>(true, "Access granted", true);
    }

    // ========== HELPER METHODS ==========

    private UserProgress.LessonProgress findOrCreateLessonProgress(UserProgress progress, String lessonId) {
        if (progress.getLessonsProgress() == null) {
            progress.setLessonsProgress(new ArrayList<>());
        }

        return progress.getLessonsProgress().stream()
                .filter(lp -> lp.getLessonId().equals(lessonId))
                .findFirst()
                .orElseGet(() -> {
                    UserProgress.LessonProgress newLp = UserProgress.LessonProgress.builder()
                            .lessonId(lessonId)
                            .completed(false)
                            .timeSpent(0)
                            .videoProgress(0)
                            .quizAttempts(0)
                            .build();
                    progress.getLessonsProgress().add(newLp);
                    return newLp;
                });
    }

    private void updateTotalProgress(UserProgress progress, String courseId) {
        Long totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons == 0) {
            progress.setTotalProgress(0);
            return;
        }

        int completedCount = progress.getCompletedLessons() != null ? progress.getCompletedLessons().size() : 0;
        int percent = (int) ((completedCount * 100.0) / totalLessons);
        progress.setTotalProgress(percent);

        // Nếu hoàn thành 100% → set completedAt
        if (percent >= 100 && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
            log.info("User {} completed course {}", progress.getUserId(), courseId);
        }
    }

    private QuizResult gradeQuiz(Quiz quiz, QuizSubmission submission) {
        List<QuizResult.QuestionResult> results = new ArrayList<>();
        int correctCount = 0;
        int totalPoints = 0;
        int earnedPoints = 0;

        for (Quiz.Question question : quiz.getQuestions()) {
            QuizSubmission.Answer userAnswer = submission.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            List<String> correctAnswers = question.getOptions().stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                    .map(Quiz.Option::getId)
                    .collect(Collectors.toList());

            List<String> userAnswers = userAnswer != null ? userAnswer.getSelectedOptions() : new ArrayList<>();
            boolean correct = userAnswers.containsAll(correctAnswers) && correctAnswers.containsAll(userAnswers);

            if (correct) {
                correctCount++;
                earnedPoints += (question.getPoints() != null ? question.getPoints() : 1);
            }

            totalPoints += (question.getPoints() != null ? question.getPoints() : 1);

            results.add(QuizResult.QuestionResult.builder()
                    .questionId(question.getId())
                    .correct(correct)
                    .userAnswers(userAnswers)
                    .correctAnswers(correctAnswers)
                    .explanation(question.getExplanation())
                    .build());
        }

        int score = totalPoints > 0 ? (earnedPoints * 100 / totalPoints) : 0;
        boolean passed = score >= quiz.getPassingScore();

        return QuizResult.builder()
                .score(score)
                .totalQuestions(quiz.getQuestions().size())
                .correctAnswers(correctCount)
                .passed(passed)
                .results(results)
                .build();
    }
}

