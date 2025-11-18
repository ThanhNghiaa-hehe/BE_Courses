package com.example.cake.order.dto;

import com.example.cake.order.model.OrderItem;
import com.example.cake.order.model.OrderStatus;
import com.example.cake.order.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
        private String id;                   // Order ID
    private String userId;               // ID của user đặt đơn
    private String userName;             // Tên user (join từ User collection)
    private String userPhone;            // Số điện thoại user (join từ User collection)
    private List<OrderItem> items;       // Danh sách khóa học trong đơn
    private Double totalPrice;           // Tổng giá trị đơn hàng
    private Double discount;             // Giảm giá tổng đơn (%)
    private String shippingAddress;      // Địa chỉ giao hàng
    private PaymentMethod paymentMethod; // Phương thức thanh toán
    private OrderStatus status;          // Trạng thái đơn hàng
    private LocalDateTime createdAt;     // Thời gian tạo đơn
    private LocalDateTime updatedAt;     // Thời gian cập nhật đơn
}