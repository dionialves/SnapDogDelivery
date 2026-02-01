package com.dionialves.snapdogdelivery.order.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    @NotNull(message = "CLient ID is madatory")
    private Long clientId;

    @NotEmpty(message = "Order must have at least one product")
    @Size(min = 1, max = 50, message = "Order must have between {min} and {max} products")
    @Valid
    private List<ProductOrderDTO> products;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

}
