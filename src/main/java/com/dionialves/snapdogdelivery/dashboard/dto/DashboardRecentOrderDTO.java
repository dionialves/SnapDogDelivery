package com.dionialves.snapdogdelivery.dashboard.dto;

import java.math.BigDecimal;

import com.dionialves.snapdogdelivery.order.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentOrderDTO {

    private Long id;
    private String clientName;
    private String status;
    private BigDecimal totalValue;

    public static DashboardRecentOrderDTO fromEntity(Order order) {
        DashboardRecentOrderDTO dto = new DashboardRecentOrderDTO();
        dto.setId(order.getId());
        dto.setClientName(order.getClient().getName());
        dto.setStatus(order.getStatus().name());
        dto.setTotalValue(order.getTotalValue());
        return dto;
    }
}
