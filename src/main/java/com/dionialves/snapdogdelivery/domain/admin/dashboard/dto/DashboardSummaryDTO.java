package com.dionialves.snapdogdelivery.domain.admin.dashboard.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private long ordersToday;
    private long ordersYesterday;

    private BigDecimal revenueToday;
    private BigDecimal revenueYesterday;

    private long newCustomersToday;
    private long newCustomersYesterday;

    private BigDecimal averageTicketToday;
    private BigDecimal averageTicketYesterday;

    public Integer getOrdersGrowthPercentage() {

        if (ordersYesterday == 0)
            return 0;
        return (int) ((ordersToday - ordersYesterday) * 100 / ordersYesterday);
    }

    public Integer getRevenueGrowthPercentage() {

        if (revenueYesterday == null || revenueYesterday.compareTo(BigDecimal.ZERO) == 0)
            return null;

        return revenueToday
                .subtract(revenueYesterday)
                .multiply(BigDecimal.valueOf(100))
                .divide(revenueYesterday, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    public Integer getNewCustomersGrowthPercentage() {

        if (newCustomersYesterday == 0)
            return 0;
        return (int) ((newCustomersToday - newCustomersYesterday) * 100 / newCustomersYesterday);
    }

    public Integer getAverageTicketGrowthPercentage() {

        if (averageTicketYesterday == null || averageTicketYesterday.compareTo(BigDecimal.ZERO) == 0)
            return null;

        return averageTicketToday
                .subtract(averageTicketYesterday)
                .multiply(BigDecimal.valueOf(100))
                .divide(averageTicketYesterday, 0, RoundingMode.HALF_UP)
                .intValue();
    }

}
