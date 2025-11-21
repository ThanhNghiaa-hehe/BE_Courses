package com.example.cake.lesson.service;

import com.example.cake.course.model.Course;
import com.example.cake.course.repository.CourseRepository;
import com.example.cake.lesson.dto.ChapterProgressDTO;
import com.example.cake.lesson.dto.LessonCompleteResponse;
import com.example.cake.lesson.dto.MyCourseDTO;
import com.example.cake.lesson.dto.QuizResult;
import com.example.cake.lesson.dto.QuizSubmission;
import com.example.cake.lesson.model.Chapter;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.model.Quiz;
import com.example.cake.lesson.model.UserProgress;
import com.example.cake.lesson.repository.ChapterRepository;
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
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

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
     *
     * 2 trường hợp:
     * 1. User CHƯA MUA khóa học → Chỉ xem được lessons isFree (preview)
     * 2. User ĐÃ MUA khóa học → Unlock theo thứ tự (không quan tâm isFree)
     */
    public ResponseMessage<Boolean> canAccessLesson(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        // Kiểm tra user đã enroll khóa học chưa
        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, lesson.getCourseId()).orElse(null);

        if (progress == null) {
            // User CHƯA MUA khóa học → Chỉ cho xem lessons miễn phí (preview)
            if (Boolean.TRUE.equals(lesson.getIsFree())) {
                return new ResponseMessage<>(true, "Access granted (free preview lesson)", true);
            } else {
                return new ResponseMessage<>(false, "User not enrolled in this course", false);
            }
        }

        // User ĐÃ MUA khóa học → Check unlock tuần tự
        // (Không quan tâm isFree, vì đã mua rồi thì tất cả lessons đều có quyền)

        // Nếu không có yêu cầu lesson trước → unlock (lesson đầu)
        if (lesson.getRequiredPreviousLesson() == null || lesson.getRequiredPreviousLesson().isEmpty()) {
            return new ResponseMessage<>(true, "Access granted", true);
        }

        // Kiểm tra lesson trước đã hoàn thành chưa (unlock tuần tự)
        if (!progress.isLessonCompleted(lesson.getRequiredPreviousLesson())) {
            return new ResponseMessage<>(false, "Previous lesson not completed. Please complete the required lesson first.", false);
        }

        return new ResponseMessage<>(true, "Access granted", true);
    }

    /**
     * Lấy thông tin lesson tiếp theo (API endpoint)
     */
    public ResponseMessage<LessonCompleteResponse> getNextLessonInfo(String userId, String currentLessonId) {
        Lesson currentLesson = lessonRepository.findById(currentLessonId).orElse(null);
        if (currentLesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, currentLesson.getCourseId()).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "Progress not found", null);
        }

        LessonCompleteResponse response = createCompleteResponse(progress, currentLesson.getCourseId());
        return new ResponseMessage<>(true, "Next lesson info retrieved", response);
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

    /**
     * Tìm lesson tiếp theo sau khi complete
     * CHỈ trả về lesson ĐÃ UNLOCK (có thể truy cập được)
     */
    private Lesson findNextLesson(Lesson currentLesson, UserProgress progress) {
        // 1. Tìm tất cả lessons sau current lesson trong cùng chapter
        List<Lesson> candidatesInChapter = lessonRepository.findByChapterIdAndOrderGreaterThanOrderByOrderAsc(
                currentLesson.getChapterId(),
                currentLesson.getOrder()
        );

        // 2. Tìm lesson đầu tiên đã unlock trong chapter
        for (Lesson lesson : candidatesInChapter) {
            if (isLessonUnlocked(lesson, progress)) {
                return lesson;
            }
        }

        // 3. Nếu không có lesson nào unlock trong chapter → Tìm chapter tiếp theo
        Chapter currentChapter = chapterRepository.findById(currentLesson.getChapterId()).orElse(null);
        if (currentChapter == null) return null;

        Chapter nextChapter = chapterRepository.findFirstByCourseIdAndOrderGreaterThanOrderByOrderAsc(
                currentChapter.getCourseId(),
                currentChapter.getOrder()
        );

        if (nextChapter == null) return null;

        // 4. Tìm lesson đầu tiên unlock trong chapter mới
        List<Lesson> candidatesInNextChapter = lessonRepository.findAllByChapterIdOrderByOrderAsc(nextChapter.getId());
        for (Lesson lesson : candidatesInNextChapter) {
            if (isLessonUnlocked(lesson, progress)) {
                return lesson;
            }
        }

        return null;  // Không có lesson nào unlock
    }

    /**
     * Kiểm tra lesson có unlock không (cho user ĐÃ ENROLL)
     *
     * Lưu ý: User đã mua khóa học rồi, nên KHÔNG check isFree
     * Chỉ check unlock tuần tự theo requiredPreviousLesson
     */
    private boolean isLessonUnlocked(Lesson lesson, UserProgress progress) {
        // Nếu không có yêu cầu lesson trước → unlock (lesson đầu tiên của chapter/course)
        if (lesson.getRequiredPreviousLesson() == null || lesson.getRequiredPreviousLesson().isEmpty()) {
            return true;
        }

        // Kiểm tra lesson trước đã complete chưa (unlock tuần tự)
        return progress.isLessonCompleted(lesson.getRequiredPreviousLesson());
    }

    /**
     * Tạo response khi complete lesson với thông tin lesson tiếp theo
     */
    public LessonCompleteResponse createCompleteResponse(UserProgress progress, String courseId) {
        Long totalLessons = lessonRepository.countByCourseId(courseId);
        int completedCount = progress.getCompletedLessons() != null ? progress.getCompletedLessons().size() : 0;
        boolean courseCompleted = progress.getTotalProgress() != null && progress.getTotalProgress() >= 100;

        // Tìm lesson hiện tại
        Lesson currentLesson = lessonRepository.findById(progress.getCurrentLessonId()).orElse(null);
        LessonCompleteResponse.NextLesson nextLessonInfo = null;
        String message;
        String suggestedAction = null;
        String requiredLessonId = null;

        if (courseCompleted) {
            message = "🎉 Chúc mừng! Bạn đã hoàn thành khóa học!";
            suggestedAction = "COURSE_DONE";
        } else if (currentLesson != null) {
            // Tìm lesson tiếp theo ĐÃ UNLOCK
            Lesson nextLesson = findNextLesson(currentLesson, progress);

            if (nextLesson != null) {
                // Có lesson unlock tiếp theo
                Chapter nextChapter = chapterRepository.findById(nextLesson.getChapterId()).orElse(null);
                String chapterTitle = nextChapter != null ? nextChapter.getTitle() : "";

                nextLessonInfo = LessonCompleteResponse.NextLesson.fromLesson(
                    nextLesson,
                    chapterTitle,
                    true  // Luôn true vì đã filter unlock rồi
                );

                if (Boolean.TRUE.equals(currentLesson.getHasQuiz())) {
                    message = "✅ Quiz hoàn thành! Chuyển sang bài tiếp theo.";
                } else {
                    message = "✅ Lesson hoàn thành! Chuyển sang bài tiếp theo.";
                }
            } else {
                // Không có lesson unlock tiếp theo
                if (Boolean.TRUE.equals(currentLesson.getHasQuiz())) {
                    // Kiểm tra xem quiz có pass không
                    UserProgress.LessonProgress lessonProgress = findOrCreateLessonProgress(progress, currentLesson.getId());
                    boolean quizPassed = lessonProgress.getQuizPassedAt() != null;

                    if (!quizPassed) {
                        // Quiz chưa pass
                        message = "❌ Bạn cần đạt điểm tối thiểu để unlock lesson tiếp theo. Hãy làm lại quiz!";
                        suggestedAction = "RETAKE_QUIZ";
                        requiredLessonId = currentLesson.getId();
                    } else {
                        // Quiz đã pass nhưng vẫn chưa có lesson unlock (có thể hết khóa học hoặc cần complete lesson khác)
                        message = "✅ Quiz hoàn thành! Hãy hoàn thành các bài yêu cầu khác để tiếp tục.";
                        suggestedAction = "COMPLETE_REQUIRED";
                    }
                } else {
                    // Lesson bình thường, có lesson tiếp nhưng bị lock
                    Lesson nextLockedLesson = findNextLessonIgnoreLock(currentLesson);
                    if (nextLockedLesson != null && nextLockedLesson.getRequiredPreviousLesson() != null) {
                        message = "⚠️ Hãy hoàn thành bài yêu cầu để unlock lesson tiếp theo.";
                        suggestedAction = "COMPLETE_REQUIRED";
                        requiredLessonId = nextLockedLesson.getRequiredPreviousLesson();
                    } else {
                        message = "✅ Lesson hoàn thành!";
                    }
                }
            }
        } else {
            message = "✅ Lesson hoàn thành!";
        }

        return LessonCompleteResponse.builder()
                .completed(true)
                .totalProgress(progress.getTotalProgress())
                .completedLessons(completedCount)
                .totalLessons(totalLessons.intValue())
                .nextLesson(nextLessonInfo)
                .message(message)
                .courseCompleted(courseCompleted)
                .suggestedAction(suggestedAction)
                .requiredLessonId(requiredLessonId)
                .build();
    }

    /**
     * Tìm lesson tiếp theo KHÔNG CHECK LOCK (để biết lesson nào đang block)
     */
    private Lesson findNextLessonIgnoreLock(Lesson currentLesson) {
        Lesson nextInChapter = lessonRepository.findFirstByChapterIdAndOrderGreaterThanOrderByOrderAsc(
                currentLesson.getChapterId(),
                currentLesson.getOrder()
        );

        if (nextInChapter != null) {
            return nextInChapter;
        }

        // Tìm chapter tiếp theo
        Chapter currentChapter = chapterRepository.findById(currentLesson.getChapterId()).orElse(null);
        if (currentChapter == null) return null;

        Chapter nextChapter = chapterRepository.findFirstByCourseIdAndOrderGreaterThanOrderByOrderAsc(
                currentChapter.getCourseId(),
                currentChapter.getOrder()
        );

        if (nextChapter == null) return null;

        return lessonRepository.findFirstByChapterIdOrderByOrderAsc(nextChapter.getId());
    }

    // ========== MY COURSES APIS ==========

    /**
     * Lấy danh sách khóa học user đã đăng ký (My Courses)
     */
    public ResponseMessage<java.util.List<MyCourseDTO>> getMyCourses(String userId) {
        // Lấy tất cả progress của user
        java.util.List<UserProgress> progressList = progressRepository.findByUserId(userId);

        if (progressList == null || progressList.isEmpty()) {
            return new ResponseMessage<>(true, "No enrolled courses", new java.util.ArrayList<>());
        }

        java.util.List<MyCourseDTO> myCourses = new java.util.ArrayList<>();

        for (UserProgress progress : progressList) {
            // Lấy thông tin course
            Course course = courseRepository.findById(progress.getCourseId()).orElse(null);
            if (course == null) continue;

            // Đếm tổng lessons
            Long totalLessons = lessonRepository.countByCourseId(progress.getCourseId());

            // Lấy tên lesson đang học
            String currentLessonTitle = null;
            if (progress.getCurrentLessonId() != null) {
                Lesson currentLesson = lessonRepository.findById(progress.getCurrentLessonId()).orElse(null);
                if (currentLesson != null) {
                    currentLessonTitle = currentLesson.getTitle();
                }
            }

            MyCourseDTO dto = MyCourseDTO.from(course, progress, totalLessons, currentLessonTitle);
            myCourses.add(dto);
        }

        // Sắp xếp theo lastAccessedAt (mới nhất trước)
        myCourses.sort((a, b) -> {
            if (a.getLastAccessedAt() == null) return 1;
            if (b.getLastAccessedAt() == null) return -1;
            return b.getLastAccessedAt().compareTo(a.getLastAccessedAt());
        });

        return new ResponseMessage<>(true, "My courses retrieved successfully", myCourses);
    }

    /**
     * Lấy danh sách chapters kèm progress của user
     */
    public ResponseMessage<java.util.List<ChapterProgressDTO>> getChaptersWithProgress(String userId, String courseId) {
        // Lấy progress của user
        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        if (progress == null) {
            return new ResponseMessage<>(false, "User not enrolled in this course", null);
        }

        // Lấy tất cả chapters
        java.util.List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderAsc(courseId);
        if (chapters == null || chapters.isEmpty()) {
            return new ResponseMessage<>(false, "No chapters found", null);
        }

        java.util.List<ChapterProgressDTO> result = new java.util.ArrayList<>();

        for (Chapter chapter : chapters) {
            // Đếm lessons đã hoàn thành trong chapter
            java.util.List<Lesson> lessonsInChapter = lessonRepository.findAllByChapterIdOrderByOrderAsc(chapter.getId());
            int completedCount = 0;
            String finalQuizId = null;
            Boolean quizPassed = null;
            Integer quizScore = null;

            for (Lesson lesson : lessonsInChapter) {
                if (progress.isLessonCompleted(lesson.getId())) {
                    completedCount++;
                }

                // Tìm quiz cuối chapter (lesson có hasQuiz = true và order lớn nhất)
                if (Boolean.TRUE.equals(lesson.getHasQuiz())) {
                    finalQuizId = lesson.getId();

                    // Lấy quiz progress
                    UserProgress.LessonProgress lessonProgress = progress.getLessonsProgress() != null ?
                        progress.getLessonsProgress().stream()
                            .filter(lp -> lp.getLessonId().equals(lesson.getId()))
                            .findFirst()
                            .orElse(null) : null;

                    if (lessonProgress != null) {
                        quizPassed = lessonProgress.getQuizPassedAt() != null;
                        quizScore = lessonProgress.getQuizScore();
                    }
                }
            }

            // Check chapter unlock
            Boolean isUnlocked = isChapterUnlocked(chapter, progress, chapters);

            ChapterProgressDTO dto = ChapterProgressDTO.from(
                chapter,
                isUnlocked,
                completedCount,
                finalQuizId,
                quizPassed,
                quizScore
            );

            result.add(dto);
        }

        return new ResponseMessage<>(true, "Chapters with progress retrieved", result);
    }

    /**
     * Check xem chapter có unlock không
     */
    private Boolean isChapterUnlocked(Chapter chapter, UserProgress progress, java.util.List<Chapter> allChapters) {
        // Chapter đầu tiên luôn unlock
        if (chapter.getOrder() == 1) {
            return true;
        }

        // Tìm chapter trước
        Chapter previousChapter = allChapters.stream()
            .filter(c -> c.getOrder().equals(chapter.getOrder() - 1))
            .findFirst()
            .orElse(null);

        if (previousChapter == null) {
            return true; // Không có chapter trước → unlock
        }

        // Tìm quiz cuối chapter trước
        java.util.List<Lesson> lessonsInPreviousChapter = lessonRepository.findAllByChapterIdOrderByOrderAsc(previousChapter.getId());
        Lesson finalQuiz = lessonsInPreviousChapter.stream()
            .filter(l -> Boolean.TRUE.equals(l.getHasQuiz()))
            .reduce((first, second) -> second) // Lấy quiz cuối cùng
            .orElse(null);

        if (finalQuiz != null) {
            // Cần pass quiz cuối chapter trước
            return progress.isLessonCompleted(finalQuiz.getId());
        } else {
            // Không có quiz → Chỉ cần complete tất cả lessons
            long completedInPrevious = lessonsInPreviousChapter.stream()
                .filter(l -> progress.isLessonCompleted(l.getId()))
                .count();
            return completedInPrevious == lessonsInPreviousChapter.size();
        }
    }
}

