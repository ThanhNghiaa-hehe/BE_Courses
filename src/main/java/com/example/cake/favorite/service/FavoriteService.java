package com.example.cake.favorite.service;

import com.example.cake.auth.repository.UserRepository;
import com.example.cake.course.repository.CourseRepository;
import com.example.cake.favorite.dto.FavoriteRequest;
import com.example.cake.favorite.dto.FavoriteResponse;
import com.example.cake.favorite.model.Favorite;
import com.example.cake.favorite.model.FavoriteItem;
import com.example.cake.favorite.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CourseRepository productRepository;
    private final UserRepository userRepository;

    // Thêm sản phẩm vào danh sách yêu thích
    public FavoriteResponse addToFavorite(String userId, FavoriteRequest request) {
        // 1. Kiểm tra userId tồn tại
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // 2. Kiểm tra productId tồn tại
        var productOptional = productRepository.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        // 3. Tìm bản ghi Favorite của user
        Favorite favorite = favoriteRepository.findByUserId(userId).stream().findFirst().orElse(null);

        // 4. Nếu chưa có bản ghi -> tạo mới
        if (favorite == null) {
            FavoriteItem favoriteItem = FavoriteItem.builder()
                    .productId(request.getProductId())
                    .name(request.getName())
                    .image(request.getImage())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .selected(false)
                    .discount(request.getDiscount())
                    .build();

            favorite = Favorite.builder()
                    .userId(userId)
                    .favoriteItem(new ArrayList<>(List.of(favoriteItem)))
                    .build();

            favoriteRepository.save(favorite);
            return FavoriteResponse.builder()
                    .idFavorite(favorite.getIdFavorite())
                    .userId(favorite.getUserId())
                    .favoriteItem(favorite.getFavoriteItem())
                    .build();
        }

        // 5. Nếu đã có bản ghi thì kiểm tra sản phẩm đã tồn tại chưa
        boolean exists = favorite.getFavoriteItem().stream()
                .anyMatch(item -> item.getProductId().equals(request.getProductId()));

        if (exists) {
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        }

        // 6. Thêm mới item vào list
        FavoriteItem newItem = FavoriteItem.builder()
                .productId(request.getProductId())
                .name(request.getName())
                .image(request.getImage())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .selected(false)
                .discount(request.getDiscount())
                .build();

        favorite.getFavoriteItem().add(newItem);

        Favorite saved = favoriteRepository.save(favorite);

        return FavoriteResponse.builder()
                .idFavorite(saved.getIdFavorite())
                .userId(saved.getUserId())
                .favoriteItem(saved.getFavoriteItem())
                .build();
    }



    // Lấy danh sách yêu thích của user
    public List<FavoriteResponse> getFavoritesByUserId(String userId) {
        // Kiểm tra userId tồn tại
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(favorite -> FavoriteResponse.builder()
                        .idFavorite(favorite.getIdFavorite())
                        .userId(favorite.getUserId())
                        .favoriteItem(favorite.getFavoriteItem())
                        .build())
                .collect(Collectors.toList());
    }

    // Xóa sản phẩm khỏi danh sách yêu thích
    public void removeFromFavorite(String userId, String productId) {
        // Kiểm tra userId tồn tại
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // Tìm favorite của user
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        if (favorites.isEmpty()) {
            throw new RuntimeException("Không tìm thấy danh sách yêu thích của người dùng");
        }

        Favorite favorite = favorites.get(0); // Lấy favorite đầu tiên
        
        // Tìm và xóa item khỏi list
        boolean removed = favorite.getFavoriteItem().removeIf(item -> 
            item.getProductId().equals(productId));
        
        if (!removed) {
            throw new RuntimeException("Sản phẩm không có trong danh sách yêu thích");
        }

        // Nếu list rỗng thì xóa luôn document, không thì save lại
        if (favorite.getFavoriteItem().isEmpty()) {
            favoriteRepository.delete(favorite);
        } else {
            favoriteRepository.save(favorite);
        }
    }

    // Kiểm tra sản phẩm có trong danh sách yêu thích không
    public boolean isProductInFavorite(String userId, String productId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        if (favorites.isEmpty()) {
            return false;
        }
        
        return favorites.get(0).getFavoriteItem().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
    }

    // Đếm số lượng sản phẩm yêu thích của user
    public long countFavoritesByUserId(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        if (favorites.isEmpty()) {
            return 0;
        }
        
        return favorites.get(0).getFavoriteItem().size();
    }

    // Cập nhật trạng thái selected của sản phẩm yêu thích
    public FavoriteResponse updateSelectedStatus(String userId, String productId, boolean selected) {
        // Kiểm tra userId tồn tại
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // Tìm favorite của user
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        if (favorites.isEmpty()) {
            throw new RuntimeException("Không tìm thấy danh sách yêu thích của người dùng");
        }

        Favorite favorite = favorites.get(0);
        
        // Tìm và cập nhật trạng thái selected
        boolean found = false;
        for (FavoriteItem item : favorite.getFavoriteItem()) {
            if (item.getProductId().equals(productId)) {
                item.setSelected(selected);
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new RuntimeException("Sản phẩm không có trong danh sách yêu thích");
        }

        Favorite saved = favoriteRepository.save(favorite);
        
        return FavoriteResponse.builder()
                .idFavorite(saved.getIdFavorite())
                .userId(saved.getUserId())
                .favoriteItem(saved.getFavoriteItem())
                .build();
    }

    // Xóa tất cả sản phẩm yêu thích của user
    public void clearFavorites(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        favoriteRepository.deleteAll(favorites);
    }
}
