package com.dionialves.snapdogdelivery.domain.admin.product.dto;

import java.math.BigDecimal;

import com.dionialves.snapdogdelivery.domain.admin.product.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private boolean active;

    public static ProductResponseDTO fromEntity(Product product) {

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.isActive());

    }
}
