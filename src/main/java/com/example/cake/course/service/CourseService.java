package com.example.cake.course.service;

import com.example.cake.course.dto.*;
import com.example.cake.course.model.Course;
import com.example.cake.course.repository.CourseRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public ResponseMessage<Course> createCourse(CourseCreateRequest request) {

        // Build Course dựa trên model hiện tại (title, thumbnailUrl, isPublished, price: Double)
        Course course = Course.builder()
                .categoryCode(request.getCategoryCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())               // Double -> dùng trực tiếp
                .thumbnailUrl(request.getThumbnailUrl())
                .duration(request.getDuration())
                .level(request.getLevel())
                .isPublished(request.getIsPublished())
                .build();

        courseRepository.save(course);

        return new ResponseMessage<>(true, "Course created", course);
    }

    public ResponseMessage<List<Course>> getAll() {
        return new ResponseMessage<>(true, "Success", courseRepository.findAll());
    }

    public ResponseMessage<Course> getById(String id) {
        Course course = courseRepository.findById(id).orElse(null);

        if (course == null) {
            return new ResponseMessage<>(false, "Course not found", null);
        }

        return new ResponseMessage<>(true, "Success", course);
    }

    public ResponseMessage<Course> updateCourse(CourseUpdateRequest request) {
        Course course = courseRepository.findById(request.getId()).orElse(null);
        if (course == null) {
            return new ResponseMessage<>(false, "Course not found", null);
        }

        if (request.getCategoryCode() != null) course.setCategoryCode(request.getCategoryCode());
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice()); // Double
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDuration() != null) course.setDuration(request.getDuration());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getIsPublished() != null) course.setIsPublished(request.getIsPublished());

        courseRepository.save(course);
        return new ResponseMessage<>(true, "Updated", course);
    }


    public ResponseMessage<String> deleteCourse(String id) {

        if (!courseRepository.existsById(id)) {
            return new ResponseMessage<>(false, "Course not found", null);
        }

        courseRepository.deleteById(id);

        return new ResponseMessage<>(true, "Deleted", id);
    }

    public ResponseMessage<List<Course>> getAllPublished() {
        List<Course> published = courseRepository.findByIsPublishedTrue();
        return new ResponseMessage<>(true, "Success", published);
    }
}
