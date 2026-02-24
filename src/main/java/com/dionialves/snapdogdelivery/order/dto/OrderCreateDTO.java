package com.dionialves.snapdogdelivery.order.dto;

import java.time.LocalDateTime;
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

import com.dionialves.snapdogdelivery.order.OrderOrigin;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clientId;

    @NotEmpty(message = "O pedido deve ter pelo menos um produto")
    @Size(min = 1, max = 50, message = "O pedido deve ter entre {min} e {max} produtos")
    @Valid
    private List<ProductOrderDTO> products;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    private OrderOrigin origin = OrderOrigin.MANUAL;
}
