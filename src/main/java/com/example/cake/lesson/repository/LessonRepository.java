package com.example.cake.lesson.repository;

import com.example.cake.lesson.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    /**
     * Tìm tất cả lesson của một chapter (sắp xếp theo order)
     */
    List<Lesson> findAllByChapterIdOrderByOrderAsc(String chapterId);

    /**
     * Tìm tất cả lesson của một khóa học
     */
    List<Lesson> findByCourseIdOrderByOrderAsc(String courseId);

    /**
     * Tìm lesson theo chapterId và order
     */
    Lesson findByChapterIdAndOrder(String chapterId, Integer order);

    /**
     * Tìm các lesson miễn phí của một khóa học
     */
    List<Lesson> findByCourseIdAndIsFreeTrue(String courseId);

    /**
     * Đếm số lesson của một chapter
     */
    Long countByChapterId(String chapterId);

    /**
     * Đếm số lesson của một khóa học
     */
    Long countByCourseId(String courseId);

    /**
     * Xóa tất cả lesson của một chapter
     */
    void deleteByChapterId(String chapterId);

    /**
     * Xóa tất cả lesson của một course
     */
    void deleteByCourseId(String courseId);

    /**
     * Tìm lesson cuối cùng trong chapter (order cao nhất)
     */
    Lesson findFirstByChapterIdOrderByOrderDesc(String chapterId);

    /**
     * Tìm lesson đầu tiên trong chapter
     */
    Lesson findFirstByChapterIdOrderByOrderAsc(String chapterId);

    /**
     * Tìm lessons trong chapter với order lớn hơn order cho trước
     */
    List<Lesson> findByChapterIdAndOrderGreaterThanOrderByOrderAsc(String chapterId, Integer order);

    /**
     * Tìm lesson đầu tiên trong chapter với order lớn hơn order cho trước
     */
    Lesson findFirstByChapterIdAndOrderGreaterThanOrderByOrderAsc(String chapterId, Integer order);
}

