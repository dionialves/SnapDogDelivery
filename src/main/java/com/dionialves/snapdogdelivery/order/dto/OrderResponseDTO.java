package com.dionialves.snapdogdelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientDTO;
import com.dionialves.snapdogdelivery.order.Order;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private ClientDTO client;
    private List<ProductOrderDTO> products;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    private BigDecimal totalValue;

    public static OrderResponseDTO fromEntity(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalValue(order.getTotalValue());

        // Mapeia Client
        dto.setClient(ClientDTO.fromEntity(order.getClient()));

        // Mapeia ProductOrders
        List<ProductOrderDTO> products = order.getProductOrders().stream()
                .map(ProductOrderDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setProducts(products);

        dto.setStatus(order.getStatus().name());

        return dto;
    }

}
