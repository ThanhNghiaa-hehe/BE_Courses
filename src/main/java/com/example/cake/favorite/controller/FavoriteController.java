package com.example.cake.favorite.controller;

import com.example.cake.favorite.dto.FavoriteRequest;
import com.example.cake.favorite.dto.FavoriteResponse;
import com.example.cake.favorite.service.FavoriteService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites/")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")

public class FavoriteController {
    private final FavoriteService favoriteService;


    @PostMapping("/{userId}")

    public ResponseEntity<ResponseMessage<FavoriteResponse>> addToFavorite(
            @PathVariable String userId,
            @RequestBody FavoriteRequest request) {
        try {
            FavoriteResponse response = favoriteService.addToFavorite(userId, request);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Đã thêm khóa học vào danh sách yêu thích", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Lấy danh sách yêu thích của user
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage<List<FavoriteResponse>>> getFavoritesByUserId(
            @PathVariable String userId) {
        try {
            List<FavoriteResponse> favorites = favoriteService.getFavoritesByUserId(userId);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Lấy danh sách yêu thích thành công", favorites));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Xóa khóa học khỏi danh sách yêu thích
    @DeleteMapping("/{userId}/{courseId}")
    public ResponseEntity<ResponseMessage<Void>> removeFromFavorite(
            @PathVariable String userId,
            @PathVariable String courseId) {
        try {
            favoriteService.removeFromFavorite(userId, courseId);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Đã xóa khóa học khỏi danh sách yêu thích", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Kiểm tra khóa học có trong danh sách yêu thích không
    @GetMapping("/{userId}/check/{courseId}")
    public ResponseEntity<ResponseMessage<Boolean>> checkCourseInFavorite(
            @PathVariable String userId,
            @PathVariable String courseId) {
        try {
            boolean isInFavorite = favoriteService.isProductInFavorite(userId, courseId);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Kiểm tra thành công", isInFavorite));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Đếm số lượng khóa học yêu thích
    @GetMapping("/{userId}/count")
    public ResponseEntity<ResponseMessage<Long>> countFavorites(
            @PathVariable String userId) {
        try {
            long count = favoriteService.countFavoritesByUserId(userId);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Đếm thành công", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Cập nhật trạng thái selected
    @PutMapping("/{userId}/{courseId}/select")
    public ResponseEntity<ResponseMessage<FavoriteResponse>> updateSelectedStatus(
            @PathVariable String userId,
            @PathVariable String courseId,
            @RequestParam boolean selected) {
        try {
            FavoriteResponse response = favoriteService.updateSelectedStatus(userId, courseId, selected);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Cập nhật trạng thái thành công", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }

    // Xóa tất cả khóa học yêu thích
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ResponseMessage<Void>> clearFavorites(
            @PathVariable String userId) {
        try {
            favoriteService.clearFavorites(userId);
            return ResponseEntity.ok(new ResponseMessage<>(true, "Đã xóa tất cả khóa học yêu thích", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage<>(false, "Lỗi server: " + e.getMessage(), null));
        }
    }
}