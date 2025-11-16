package com.example.cake.course.service;

import com.example.cake.course.dto.*;
import com.example.cake.course.model.CourseCategory;
import com.example.cake.course.repository.CourseCategoryRepository;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCategoryService {

    private final CourseCategoryRepository categoryRepository;

    public ResponseMessage<CourseCategory> createCategory(CourseCategoryCreateRequest request) {

        // Check trùng mã
        if (categoryRepository.findByCode(request.getCode()).isPresent()) {
            return new ResponseMessage<>(false, "Category code already exists", null);
        }

        CourseCategory category = CourseCategory.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        categoryRepository.save(category);

        return new ResponseMessage<>(true, "Created successfully", category);
    }

    public ResponseMessage<List<CourseCategory>> getAll() {
        return new ResponseMessage<>(true, "Success", categoryRepository.findAll());
    }

    public ResponseMessage<CourseCategory> updateCategory(CourseCategoryUpdateRequest request) {

        CourseCategory category = categoryRepository.findById(request.getId())
                .orElse(null);

        if (category == null) {
            return new ResponseMessage<>(false, "Category not found", null);
        }

        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        categoryRepository.save(category);

        return new ResponseMessage<>(true, "Updated successfully", category);
    }

    public ResponseMessage<String> deleteCategory(String code) {

        CourseCategory category = categoryRepository.findByCode(code)
                .orElse(null);

        if (category == null) {
            return new ResponseMessage<>(false, "Category not found", null);
        }

        categoryRepository.delete(category);

        return new ResponseMessage<>(true, "Deleted successfully", code);
    }
}
