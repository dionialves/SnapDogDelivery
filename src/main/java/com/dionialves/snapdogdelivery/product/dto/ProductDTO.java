package com.dionialves.snapdogdelivery.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotEmpty(message = "Name is mandatory")
    private String name;

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private String description;

}
