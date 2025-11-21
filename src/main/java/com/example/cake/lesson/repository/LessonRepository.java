package com.example.cake.lesson.repository;

import com.example.cake.lesson.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    /**
     * Tìm tất cả lesson của một chapter
     */
    List<Lesson> findByChapterIdOrderByOrderAsc(String chapterId);

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
     * Xóa tất cả lesson của một khóa học
     */
    void deleteByCourseId(String courseId);
}

