package com.dionialves.snapdogdelivery.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.dashboard.dto.DashboardRecentOrderDTO;
import com.dionialves.snapdogdelivery.dashboard.dto.DashboardSummaryDTO;
import com.dionialves.snapdogdelivery.dashboard.dto.DashboardTopProductDTO;
import com.dionialves.snapdogdelivery.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        long ordersToday = orderRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long ordersYesterday = orderRepository.countByCreatedAtBetween(yesterdayStart, yesterdayEnd);
        summary.setOrdersToday(ordersToday);
        summary.setOrdersYesterday(ordersYesterday);

        BigDecimal revenueToday = orderRepository.sumRevenueByCreatedAtBetween(todayStart, todayEnd);
        BigDecimal revenueYesterday = orderRepository.sumRevenueByCreatedAtBetween(yesterdayStart, yesterdayEnd);
        summary.setRevenueToday(revenueToday);
        summary.setRevenueYesterday(revenueYesterday);

        summary.setNewClientsToday(clientRepository.countByCreatedAt(today));
        summary.setNewClientsYesterday(clientRepository.countByCreatedAt(yesterday));

        BigDecimal avgToday = ordersToday > 0
                ? revenueToday.divide(BigDecimal.valueOf(ordersToday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal avgYesterday = ordersYesterday > 0
                ? revenueYesterday.divide(BigDecimal.valueOf(ordersYesterday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        summary.setAverageTicketToday(avgToday);
        summary.setAverageTicketYesterday(avgYesterday);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<DashboardRecentOrderDTO> getRecentOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5))
                .stream()
                .map(DashboardRecentOrderDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardTopProductDTO> getTopSellingProducts() {
        return orderRepository.findTopSellingProducts(PageRequest.of(0, 5));
    }

}
