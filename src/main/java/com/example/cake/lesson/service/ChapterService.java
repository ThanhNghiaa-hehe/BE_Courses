package com.example.cake.lesson.service;

import com.example.cake.lesson.dto.ChapterRequest;
import com.example.cake.lesson.model.Chapter;
import com.example.cake.lesson.model.Lesson;
import com.example.cake.lesson.repository.ChapterRepository;
import com.example.cake.lesson.repository.LessonRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;

    /**
     * Tạo chapter mới
     */
    public ResponseMessage<Chapter> createChapter(ChapterRequest request) {
        Chapter chapter = Chapter.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .order(request.getOrder())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .totalLessons(0)
                .totalDuration(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        chapterRepository.save(chapter);
        log.info("Created chapter: {} for course: {}", chapter.getId(), request.getCourseId());

        return new ResponseMessage<>(true, "Chapter created successfully", chapter);
    }

    /**
     * Lấy tất cả chapter của một khóa học
     */
    public ResponseMessage<List<Chapter>> getChaptersByCourse(String courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderAsc(courseId);
        return new ResponseMessage<>(true, "Success", chapters);
    }

    /**
     * Lấy chapter theo ID
     */
    public ResponseMessage<Chapter> getChapterById(String id) {
        Chapter chapter = chapterRepository.findById(id).orElse(null);

        if (chapter == null) {
            return new ResponseMessage<>(false, "Chapter not found", null);
        }

        return new ResponseMessage<>(true, "Success", chapter);
    }

    /**
     * Cập nhật chapter
     */
    public ResponseMessage<Chapter> updateChapter(String id, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(id).orElse(null);

        if (chapter == null) {
            return new ResponseMessage<>(false, "Chapter not found", null);
        }

        if (request.getTitle() != null) chapter.setTitle(request.getTitle());
        if (request.getDescription() != null) chapter.setDescription(request.getDescription());
        if (request.getOrder() != null) chapter.setOrder(request.getOrder());
        if (request.getIsFree() != null) chapter.setIsFree(request.getIsFree());

        chapter.setUpdatedAt(LocalDateTime.now());
        chapterRepository.save(chapter);

        log.info("Updated chapter: {}", id);
        return new ResponseMessage<>(true, "Chapter updated successfully", chapter);
    }

    /**
     * Xóa chapter (và tất cả lesson trong đó)
     */
    @Transactional
    public ResponseMessage<String> deleteChapter(String id) {
        Chapter chapter = chapterRepository.findById(id).orElse(null);

        if (chapter == null) {
            return new ResponseMessage<>(false, "Chapter not found", null);
        }

        // Xóa tất cả lesson trong chapter này
        lessonRepository.deleteByChapterId(id);

        // Xóa chapter
        chapterRepository.deleteById(id);

        log.info("Deleted chapter: {} and its lessons", id);
        return new ResponseMessage<>(true, "Chapter and its lessons deleted successfully", id);
    }

    /**
     * Cập nhật thống kê chapter (tổng lesson, thời lượng)
     */
    public void updateChapterStats(String chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) return;

        Long totalLessons = lessonRepository.countByChapterId(chapterId);

        // Tính tổng thời lượng
        List<Lesson> lessons = lessonRepository.findAllByChapterIdOrderByOrderAsc(chapterId);
        Integer totalDuration = lessons.stream()
                .mapToInt(l -> l.getDuration() != null ? l.getDuration() : 0)
                .sum();

        chapter.setTotalLessons(totalLessons.intValue());
        chapter.setTotalDuration(totalDuration);
        chapter.setUpdatedAt(LocalDateTime.now());

        chapterRepository.save(chapter);
        log.info("Updated stats for chapter: {}, lessons: {}, duration: {}min", chapterId, totalLessons, totalDuration);
    }
}

