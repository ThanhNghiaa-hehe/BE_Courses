package com.example.cake.lesson.service;

import com.example.cake.lesson.dto.LessonRequest;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.repository.LessonRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterService chapterService;

    /**
     * Tạo lesson mới
     */
    public ResponseMessage<Lesson> createLesson(LessonRequest request) {
        // Extract videoId from URL if not provided
        String videoId = request.getVideoId();
        if (videoId == null && request.getVideoUrl() != null) {
            videoId = extractVideoId(request.getVideoUrl(), request.getVideoType());
        }

        Lesson lesson = Lesson.builder()
                .chapterId(request.getChapterId())
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .order(request.getOrder())
                .duration(request.getDuration())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .requiredPreviousLesson(request.getRequiredPreviousLesson())
                .videoUrl(request.getVideoUrl())
                .videoId(videoId)
                .videoType(request.getVideoType())
                .videoThumbnail(request.getVideoThumbnail())
                .contentType(request.getContentType())
                .content(request.getContent())
                .codeSnippets(request.getCodeSnippets())
                .attachments(request.getAttachments())
                .hasQuiz(request.getHasQuiz() != null ? request.getHasQuiz() : false)
                // Note: quizId will be set later when quiz is created via QuizService
                .quizId(null)
                .views(0)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lessonRepository.save(lesson);

        // Cập nhật stats của chapter
        chapterService.updateChapterStats(request.getChapterId());

        log.info("Created lesson: {} in chapter: {}", lesson.getId(), request.getChapterId());
        return new ResponseMessage<>(true, "Lesson created successfully", lesson);
    }

    /**
     * Lấy tất cả lesson của một chapter
     */
    public ResponseMessage<List<Lesson>> getLessonsByChapter(String chapterId) {
        List<Lesson> lessons = lessonRepository.findAllByChapterIdOrderByOrderAsc(chapterId);
        return new ResponseMessage<>(true, "Success", lessons);
    }

    /**
     * Lấy tất cả lesson của một khóa học
     */
    public ResponseMessage<List<Lesson>> getLessonsByCourse(String courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderAsc(courseId);
        return new ResponseMessage<>(true, "Success", lessons);
    }

    /**
     * Lấy lesson theo ID
     */
    public ResponseMessage<Lesson> getLessonById(String id) {
        Lesson lesson = lessonRepository.findById(id).orElse(null);

        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        // Tăng view count
        lesson.setViews(lesson.getViews() + 1);
        lessonRepository.save(lesson);

        return new ResponseMessage<>(true, "Success", lesson);
    }

    /**
     * Cập nhật lesson
     */
    public ResponseMessage<Lesson> updateLesson(String id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id).orElse(null);

        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getOrder() != null) lesson.setOrder(request.getOrder());
        if (request.getDuration() != null) lesson.setDuration(request.getDuration());
        if (request.getIsFree() != null) lesson.setIsFree(request.getIsFree());
        if (request.getRequiredPreviousLesson() != null) lesson.setRequiredPreviousLesson(request.getRequiredPreviousLesson());

        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
            if (request.getVideoId() == null && request.getVideoType() != null) {
                lesson.setVideoId(extractVideoId(request.getVideoUrl(), request.getVideoType()));
            }
        }
        if (request.getVideoId() != null) lesson.setVideoId(request.getVideoId());
        if (request.getVideoType() != null) lesson.setVideoType(request.getVideoType());
        if (request.getVideoThumbnail() != null) lesson.setVideoThumbnail(request.getVideoThumbnail());

        if (request.getContentType() != null) lesson.setContentType(request.getContentType());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        if (request.getCodeSnippets() != null) lesson.setCodeSnippets(request.getCodeSnippets());
        if (request.getAttachments() != null) lesson.setAttachments(request.getAttachments());

        if (request.getHasQuiz() != null) lesson.setHasQuiz(request.getHasQuiz());
        // Note: Quiz is now a separate entity
        // To add quiz to this lesson, use: POST /api/admin/quizzes/create with lessonId
        // The quizId will be automatically set when quiz is created

        lesson.setUpdatedAt(LocalDateTime.now());
        lessonRepository.save(lesson);

        // Cập nhật stats của chapter
        chapterService.updateChapterStats(lesson.getChapterId());

        log.info("Updated lesson: {}", id);
        return new ResponseMessage<>(true, "Lesson updated successfully", lesson);
    }

    /**
     * Xóa lesson
     */
    public ResponseMessage<String> deleteLesson(String id) {
        Lesson lesson = lessonRepository.findById(id).orElse(null);

        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        String chapterId = lesson.getChapterId();
        lessonRepository.deleteById(id);

        // Cập nhật stats của chapter
        chapterService.updateChapterStats(chapterId);

        log.info("Deleted lesson: {}", id);
        return new ResponseMessage<>(true, "Lesson deleted successfully", id);
    }

    /**
     * Extract video ID from URL
     */
    private String extractVideoId(String url, Lesson.VideoType type) {
        if (url == null || type == null) return null;

        try {
            switch (type) {
                case YOUTUBE:
                    // https://www.youtube.com/watch?v=VIDEO_ID
                    // https://youtu.be/VIDEO_ID
                    Pattern youtubePattern = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]+)");
                    Matcher youtubeMatcher = youtubePattern.matcher(url);
                    if (youtubeMatcher.find()) {
                        return youtubeMatcher.group(1);
                    }
                    break;

                case VIMEO:
                    // https://vimeo.com/VIDEO_ID
                    Pattern vimeoPattern = Pattern.compile("vimeo\\.com/(\\d+)");
                    Matcher vimeoMatcher = vimeoPattern.matcher(url);
                    if (vimeoMatcher.find()) {
                        return vimeoMatcher.group(1);
                    }
                    break;

                case HOSTED:
                    // For self-hosted, just return the filename
                    return url.substring(url.lastIndexOf('/') + 1);
            }
        } catch (Exception e) {
            log.error("Error extracting video ID from URL: {}", url, e);
        }

        return null;
    }

    /**
     * Tăng lượt like
     */
    public ResponseMessage<Lesson> likeLesson(String id) {
        Lesson lesson = lessonRepository.findById(id).orElse(null);

        if (lesson == null) {
            return new ResponseMessage<>(false, "Lesson not found", null);
        }

        lesson.setLikes(lesson.getLikes() + 1);
        lessonRepository.save(lesson);

        return new ResponseMessage<>(true, "Liked", lesson);
    }
}

