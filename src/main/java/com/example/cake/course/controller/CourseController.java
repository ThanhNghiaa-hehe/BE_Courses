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
import java.util.Map;
import java.io.File;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping("/upload-thumbnail")
    public ResponseEntity<?> uploadThumbnail(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File is empty"));
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = System.currentTimeMillis() + extension;

            // Lấy upload directory từ application.properties
            String uploadDir = System.getProperty("user.dir") + "/uploads/courses/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Lưu file
            File destinationFile = new File(uploadDir + filename);
            file.transferTo(destinationFile);

            // Trả về URL theo format WebConfig serve
            String fileUrl = "http://localhost:8080/static/courses/" + filename;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", fileUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
