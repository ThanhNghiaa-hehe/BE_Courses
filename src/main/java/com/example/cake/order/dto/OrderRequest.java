package com.example.cake.order.dto;

import com.example.cake.order.model.OrderItem;
import com.example.cake.order.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private String userId;               // ID của user đặt đơn
    private List<OrderItem> items;       // Danh sách khóa học trong đơn
    private Double totalPrice;           // Tổng giá trị đơn hàng (có thể null, backend sẽ tính)
    private Double discount;             // Giảm giá tổng đơn (%, VD: 5 = giảm 5%)
    private String shippingAddress;      // Địa chỉ giao hàng (email hoặc địa chỉ liên hệ)
    private PaymentMethod paymentMethod; // Phương thức thanh toán

    // Không cần status, createdAt, updatedAt - Backend tự generate
}
