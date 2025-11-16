package com.example.cake.course.repository;

import com.example.cake.course.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);
}
