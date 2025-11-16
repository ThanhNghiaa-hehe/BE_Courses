package com.example.cake.course.controller;

import com.example.cake.course.dto.*;
import com.example.cake.course.model.CourseCategory;
import com.example.cake.course.service.CourseCategoryService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/course-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseCategoryController {

    private final CourseCategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage<CourseCategory>> create(
            @RequestBody CourseCategoryCreateRequest request
    ) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseMessage<List<CourseCategory>>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseMessage<CourseCategory>> update(
            @RequestBody CourseCategoryUpdateRequest request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(request));
    }

    @DeleteMapping("/delete/{code}")
    public ResponseEntity<ResponseMessage<String>> delete(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(categoryService.deleteCategory(code));
    }
}
