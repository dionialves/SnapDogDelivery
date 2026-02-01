package com.dionialves.snapdogdelivery.productorder.dto;

import com.dionialves.snapdogdelivery.productorder.ProductOrder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderDTO {

    @NotNull(message = "Product ID is mandatory")
    private Long productId;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 20, message = "Quantity must not exceed 20")
    private Integer quantity;

    public static ProductOrderDTO fromEntity(ProductOrder productOrder) {
        return new ProductOrderDTO(productOrder.getProduct().getId(), productOrder.getQuantity());
    }
}
