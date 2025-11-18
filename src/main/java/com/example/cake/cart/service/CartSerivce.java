package com.example.cake.cart.service;


import com.example.cake.auth.model.User;
import com.example.cake.auth.repository.UserRepository;
import com.example.cake.cart.model.Cart;
import com.example.cake.cart.model.CartItem;
import com.example.cake.cart.repository.CartRepository;
import com.example.cake.response.ResponseMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartSerivce {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;



    public ResponseMessage<Cart> addToCart(String userId, CartItem newItem) {
        // Kiểm tra user có tồn tại không
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseMessage<>(false, "User không tồn tại", null);
        }
        // Tìm cart hiện tại của user
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        Cart cart;

        if (existingCart.isPresent()) {
            cart = existingCart.get();

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getCourseId().equals(newItem.getCourseId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Khóa học đã có trong giỏ hàng - không thêm nữa (khóa học không có số lượng)
                return new ResponseMessage<>(false, "Khóa học đã có trong giỏ hàng", cart);
            } else {
                // Nếu chưa có, thêm mới
                cart.getItems().add(newItem);
            }
        } else {
            // Tạo cart mới nếu chưa có
            List<CartItem> items = new ArrayList<>();
            items.add(newItem);
            cart = Cart.builder()
                    .userId(userId)
                    .items(items)
                    .build();
        }

        Cart savedCart = cartRepository.save(cart);
        return new ResponseMessage<>(true, "Thêm vào giỏ hàng thành công", savedCart);
    }

    public ResponseMessage<Cart> getCartByUserId(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseMessage<>(false, "User không tồn tại", null);
        }

        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            // Tạo cart trống nếu chưa có
            Cart emptyCart = Cart.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .build();
            return new ResponseMessage<>(true, "Giỏ hàng trống", emptyCart);
        }

        return new ResponseMessage<>(true, "Lấy giỏ hàng thành công", cart.get());
    }
    public ResponseMessage<Boolean> deleteCartItem(String userId, String courseId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseMessage<>(false, "User không tồn tại", false);
        }
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (!existingCart.isPresent()) {
            return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", false);
        }
        List<CartItem> items = existingCart.get().getItems();
        boolean itemRemoved = items.removeIf(item -> item.getCourseId().equals(courseId));
        if (!itemRemoved) {
            return new ResponseMessage<>(false, "Khóa học không tồn tại trong giỏ hàng", false);
        }
        cartRepository.save(existingCart.get());
        return new ResponseMessage<>(true, "Xóa khóa học thành công", true);
    }


    public ResponseMessage<List<CartItem>> getAllProductsInCart(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseMessage<>(false, "User không tồn tại", null);
        }
        // Tìm cart hiện tại của user
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (!existingCart.isPresent()) {
            return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", null);
        }
        // Lấy danh sách các sản phẩm trong giỏ hàng
        List<CartItem> items = existingCart.get().getItems();
        return new ResponseMessage<>(true, "Danh sách sản phẩm trong giỏ hàng", items);
    }

    public ResponseMessage<Double> getTotalPriceOfCart(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseMessage<>(false, "User không tồn tại", null);
        }

        // Tìm cart hiện tại của user
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (!existingCart.isPresent()) {
            return new ResponseMessage<>(false, "Không tìm thấy giỏ hàng", null);
        }
        // Lấy danh sách các sản phẩm trong giỏ hàng
        List<CartItem> items = existingCart.get().getItems();
        // Tính tổng giá trị của tất cả sản phẩm trong giỏ hàng
        double totalPrice = items.stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        return new ResponseMessage<>(true, "Tổng giá trị giỏ hàng", totalPrice);
    }

    public ResponseMessage<List<Cart>> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        return new ResponseMessage<>(true, "Get all carts successfully", carts);
    }

    public boolean deleteCartItemsByProductIds(String userId, List<String> courseIds){
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        // Tìm cart hiện tại của user
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (!existingCart.isPresent()) {
            return false;
        }
        List<CartItem> items = existingCart.get().getItems();

        for (String id : courseIds) {
            items.removeIf(item -> item.getCourseId().equals(id));
        }
        cartRepository.save(existingCart.get());
        return true;
    }


}






