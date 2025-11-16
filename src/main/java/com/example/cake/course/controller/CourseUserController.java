package com.example.cake.course.controller;

import com.example.cake.course.model.Course;
import com.example.cake.course.service.CourseUserService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseUserController {

    private final CourseUserService userService;

    @GetMapping
    public ResponseEntity<ResponseMessage<List<Course>>> getAllPublishedCourses() {
        return ResponseEntity.ok(userService.getAllPublishedCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Optional<Course>>> getCourseById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getCourseById(id));
    }
}
