package com.example.cake.category.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private String id;
    private String categoryCode; // Reference to Categories
    private String name;
    private String description;
    private BigDecimal price;
    private String images;
    private Integer stock; // Số lượng tồn kho
    private String ingredients; // Thành phần
    private String size; // Kích thước (Small, Medium, Large)
    private Double weight; // Trọng lượng (gram)
    private Boolean isAvailable; // Còn bán hay không
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double discountPercentage; // Ví dụ: 0.1 cho 10%, 0.25 cho 25%
}