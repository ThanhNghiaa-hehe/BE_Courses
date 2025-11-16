package com.example.cake.course.service;

import com.example.cake.course.model.Course;
import com.example.cake.course.repository.CourseRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseUserService {

    private final CourseRepository courseRepository;

    public ResponseMessage<List<Course>> getAllPublishedCourses() {
        List<Course> list = courseRepository.findByIsPublishedTrue();
        return new ResponseMessage<>(true, "Success", list);
    }

    public ResponseMessage<Optional<Course>> getCourseById(String id) {
        return new ResponseMessage<>(
                true,
                "Success",
                courseRepository.findById(id)
        );
    }
}
