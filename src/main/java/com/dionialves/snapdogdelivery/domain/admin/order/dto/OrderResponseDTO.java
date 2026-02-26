package com.dionialves.snapdogdelivery.domain.admin.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.Order;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderOrigin;
import com.dionialves.snapdogdelivery.domain.admin.productorder.dto.ProductOrderDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private CustomerDTO customer;
    private List<ProductOrderDTO> products;
    private String status;
    private OrderOrigin origin;
    private String deliveryAddress;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    private BigDecimal totalValue;

    public static OrderResponseDTO fromEntity(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalValue(order.getTotalValue());

        // Mapeia Customer
        dto.setCustomer(CustomerDTO.fromEntity(order.getCustomer()));

        // Mapeia ProductOrders
        List<ProductOrderDTO> products = order.getProductOrders().stream()
                .map(ProductOrderDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setProducts(products);

        dto.setStatus(order.getStatus().name());
        dto.setOrigin(order.getOrigin());
        dto.setDeliveryAddress(order.getDeliveryAddress());

        return dto;
    }

}
