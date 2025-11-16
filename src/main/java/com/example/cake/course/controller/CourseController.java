package com.example.cake.course.controller;

import com.example.cake.course.dto.*;
import com.example.cake.course.model.Course;
import com.example.cake.course.service.CourseService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage<Course>> createCourse(
            @RequestBody CourseCreateRequest request
    ) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseMessage<List<Course>>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Course>> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseMessage<Course>> updateCourse(
            @RequestBody CourseUpdateRequest request
    ) {
        return ResponseEntity.ok(courseService.updateCourse(request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage<String>> deleteCourse(@PathVariable String id) {
        return ResponseEntity.ok(courseService.deleteCourse(id));
    }
}
