package com.example.cake.course.repository;

import com.example.cake.course.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    // lấy khóa học đã publish
    List<Course> findByIsPublishedTrue();

    // thêm nếu cần: theo category
    List<Course> findByCategoryCode(String categoryCode);
}
