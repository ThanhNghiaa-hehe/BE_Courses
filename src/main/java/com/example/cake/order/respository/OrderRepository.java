package com.example.cake.order.respository;

import com.example.cake.order.model.Order;
import com.example.cake.order.model.OrderStatus;
import com.example.cake.order.model.PaymentMethod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository  extends MongoRepository<Order, String> {
    Optional<Order> findById(String orderId);
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findAllByStatus(OrderStatus status);
    List<Order> findAllByPaymentMethod(PaymentMethod paymentMethod);
    List<Order> findAllByShippingAddressContainingIgnoreCase(String address);
    List<Order> findAllByTotalPriceGreaterThan(double minPrice);
    List<Order> findAllByDiscountGreaterThan(double minDiscount);
    long countAllBy();
    long countAllByStatus(OrderStatus status);
}
