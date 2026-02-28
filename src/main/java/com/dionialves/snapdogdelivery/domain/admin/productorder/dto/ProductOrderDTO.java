package com.dionialves.snapdogdelivery.domain.admin.productorder.dto;

import com.dionialves.snapdogdelivery.domain.admin.productorder.ProductOrder;

import java.math.BigDecimal;

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

    @NotNull(message = "ID do produto é obrigatório")
    private Long productId;

    private String productName;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @Max(value = 20, message = "Quantidade não pode exceder 20")
    private Integer quantity;

    private BigDecimal priceAtTime;

    public static ProductOrderDTO fromEntity(ProductOrder productOrder) {
        ProductOrderDTO dto = new ProductOrderDTO();
        dto.setProductId(productOrder.getProduct().getId());
        dto.setProductName(productOrder.getProduct().getName());
        dto.setQuantity(productOrder.getQuantity());
        dto.setPriceAtTime(productOrder.getPriceAtTime());
        return dto;
    }
}
