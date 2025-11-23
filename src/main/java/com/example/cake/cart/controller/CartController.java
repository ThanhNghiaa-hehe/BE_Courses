package com.example.cake.cart.controller;

import com.example.cake.cart.service.CartSerivce;
import com.example.cake.cart.service.CartSerivce;
import com.example.cake.cart.model.Cart;
import com.example.cake.cart.model.CartItem;
import com.example.cake.response.ResponseMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DEPRECATED: Cart functionality replaced by direct payment
 * This controller is disabled - use PaymentController instead
 */
//@RestController
//@RequestMapping("api/cart")
@AllArgsConstructor
public class CartController {

    private final CartSerivce cartService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/all")
    public ResponseEntity<ResponseMessage<List<Cart>>> getAllCarts() {
        ResponseMessage<List<Cart>> response = cartService.getAllCarts();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/add/{userId}")
    public ResponseEntity<ResponseMessage<Cart>> addToCart(
            @PathVariable String userId,
            @RequestBody CartItem cartItem) {
        ResponseMessage<Cart> response = cartService.addToCart(userId, cartItem);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage<Cart>> getCart(@PathVariable String userId) {
        ResponseMessage<Cart> response = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/{userId}/item/{courseId}")
    public ResponseEntity<ResponseMessage<Boolean>> deleteCartItem(
            @PathVariable String userId,
            @PathVariable String courseId) {
        ResponseMessage<Boolean> response = cartService.deleteCartItem(userId, courseId);

            System.out.println(" respon : "+  response.isSuccess());
            return ResponseEntity.ok(response);

    }

}