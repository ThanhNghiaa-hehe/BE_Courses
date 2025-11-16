package com.example.cake.course.repository;

import com.example.cake.course.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    List<Lesson> findByCourseCode(String courseCode);

    boolean existsByTitleAndCourseCode(String title, String courseCode);
}
