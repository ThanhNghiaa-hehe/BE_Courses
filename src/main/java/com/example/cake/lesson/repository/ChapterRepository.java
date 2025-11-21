package com.example.cake.lesson.repository;

import com.example.cake.lesson.model.Chapter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, String> {

    /**
     * Tìm tất cả chapter của một khóa học
     */
    List<Chapter> findByCourseIdOrderByOrderAsc(String courseId);

    /**
     * Tìm chapter theo courseId và order
     */
    Chapter findByCourseIdAndOrder(String courseId, Integer order);

    /**
     * Đếm số chapter của một khóa học
     */
    Long countByCourseId(String courseId);

    /**
     * Xóa tất cả chapter của một khóa học
     */
    void deleteByCourseId(String courseId);

    /**
     * Tìm chapter tiếp theo trong course (theo order)
     */
    Chapter findFirstByCourseIdAndOrderGreaterThanOrderByOrderAsc(String courseId, Integer currentOrder);
}

