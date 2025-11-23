package com.example.cake.order.controller;


import com.example.cake.order.dto.OrderResponse;
import com.example.cake.order.model.Order;
import com.example.cake.order.model.OrderStatus;
import com.example.cake.order.service.OrderService;
import com.example.cake.response.ResponseMessage;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DEPRECATED: Order functionality replaced by direct payment
 * This controller is disabled - use PaymentController instead
 */
//@RestController
//@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping("/test")
    public String test(){
        return "Tôi là admin ";
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage<List<OrderResponse>>> getAllOrders() {
        ResponseMessage<List<OrderResponse>> response = orderService.getAllOrders();
        if (response.isSuccess()) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{orderId}/update-status")
    public ResponseEntity<ResponseMessage<Order>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus newStatus
    ) {
        ResponseMessage<Order> response = orderService.updateOrderStatus(orderId, newStatus);
        if (response.isSuccess()) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
