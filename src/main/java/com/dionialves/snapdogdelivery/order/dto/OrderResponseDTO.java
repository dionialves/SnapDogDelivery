package com.dionialves.snapdogdelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientCreateDTO;
import com.dionialves.snapdogdelivery.order.Order;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private ClientCreateDTO client;
    private List<ProductOrderDTO> products;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal totalValue;

    public static OrderResponseDTO fromEntity(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setDate(order.getDate());
        dto.setTotalValue(order.getTotalValue());

        // Mapeia Client
        dto.setClient(ClientCreateDTO.fromEntity(order.getClient()));

        // Mapeia ProductOrders
        List<ProductOrderDTO> products = order.getProductOrders().stream()
                .map(ProductOrderDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setProducts(products);

        return dto;
    }

}
