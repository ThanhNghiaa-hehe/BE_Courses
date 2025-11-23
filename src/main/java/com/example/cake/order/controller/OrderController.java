
package com.example.cake.order.controller;

import com.example.cake.order.dto.OrderResponse;
import com.example.cake.order.model.Order;
import com.example.cake.order.model.OrderStatus;
import com.example.cake.order.service.OrderService;
import com.example.cake.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DEPRECATED: Order functionality replaced by direct payment
 * This controller is disabled - use PaymentController instead
 */
//@RestController
//@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;




    @PostMapping("/create-order")
    public ResponseEntity<ResponseMessage<Order>> createOrder(@RequestBody Order order) {
        ResponseMessage<Order> response = orderService.createOrder(order);
        System.out.printf(" vô rồi ");
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage<List<OrderResponse>>> getOredersByUserId(@PathVariable String userId){
        ResponseMessage<List<OrderResponse>> response=orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseMessage<String>> cancelOrder(@PathVariable String orderId) {
        ResponseMessage<String> response = orderService.cancelOrder(orderId);
        if (response.isSuccess()) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}
