package com.dionialves.snapdogdelivery.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.client.State;
import com.dionialves.snapdogdelivery.dashboard.dto.DashboardTopProductDTO;
import com.dionialves.snapdogdelivery.order.Order;
import com.dionialves.snapdogdelivery.order.OrderRepository;
import com.dionialves.snapdogdelivery.order.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // --- getDashboardSummary ---

    @Test
    @DisplayName("getDashboardSummary retorna valores corretos para o dia de hoje")
    void getDashboardSummary_retornaValoresCorretos() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);

        when(orderRepository.countByCreatedAtBetween(eq(todayStart), any())).thenReturn(5L);
        when(orderRepository.countByCreatedAtBetween(eq(yesterdayStart), any())).thenReturn(3L);

        when(orderRepository.sumRevenueByCreatedAtBetween(eq(todayStart), any()))
                .thenReturn(new BigDecimal("150.00"));
        when(orderRepository.sumRevenueByCreatedAtBetween(eq(yesterdayStart), any()))
                .thenReturn(new BigDecimal("90.00"));

        when(clientRepository.countByCreatedAt(today)).thenReturn(2L);
        when(clientRepository.countByCreatedAt(yesterday)).thenReturn(1L);

        var summary = dashboardService.getDashboardSummary();

        assertThat(summary.getOrdersToday()).isEqualTo(5L);
        assertThat(summary.getOrdersYesterday()).isEqualTo(3L);
        assertThat(summary.getRevenueToday()).isEqualByComparingTo("150.00");
        assertThat(summary.getRevenueYesterday()).isEqualByComparingTo("90.00");
        assertThat(summary.getNewClientsToday()).isEqualTo(2L);
        assertThat(summary.getNewClientsYesterday()).isEqualTo(1L);
        assertThat(summary.getAverageTicketToday()).isEqualByComparingTo("30.00");
        assertThat(summary.getAverageTicketYesterday()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("getDashboardSummary com zero pedidos retorna ticket médio zero sem divisão por zero")
    void getDashboardSummary_semPedidos_ticketMedioZero() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(orderRepository.sumRevenueByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(clientRepository.countByCreatedAt(today)).thenReturn(0L);
        when(clientRepository.countByCreatedAt(yesterday)).thenReturn(0L);

        var summary = dashboardService.getDashboardSummary();

        assertThat(summary.getAverageTicketToday()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getAverageTicketYesterday()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getOrdersGrowthPercentage com ontem zero retorna zero (sem divisão por zero)")
    void getOrdersGrowthPercentage_ontemZero_retornaZero() {
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(orderRepository.sumRevenueByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(clientRepository.countByCreatedAt(any())).thenReturn(0L);

        var summary = dashboardService.getDashboardSummary();

        assertThat(summary.getOrdersGrowthPercentage()).isEqualTo(0);
        assertThat(summary.getNewClientsGrowthPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("getRevenueGrowthPercentage calcula percentual correto")
    void getRevenueGrowthPercentage_calculaPercentualCorreto() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(2L);
        when(orderRepository.sumRevenueByCreatedAtBetween(
                eq(today.atStartOfDay()), any()))
                .thenReturn(new BigDecimal("120.00"));
        when(orderRepository.sumRevenueByCreatedAtBetween(
                eq(yesterday.atStartOfDay()), any()))
                .thenReturn(new BigDecimal("100.00"));
        when(clientRepository.countByCreatedAt(any())).thenReturn(0L);

        var summary = dashboardService.getDashboardSummary();

        // (120 - 100) / 100 * 100 = 20%
        assertThat(summary.getRevenueGrowthPercentage()).isEqualTo(20);
    }

    // --- getRecentOrders ---

    @Test
    @DisplayName("getRecentOrders retorna até 5 pedidos recentes como DTOs")
    void getRecentOrders_retornaListaDTOs() {
        var client = criarClient();

        var order1 = criarOrder(1L, client, OrderStatus.PENDING, new BigDecimal("30.00"));
        var order2 = criarOrder(2L, client, OrderStatus.DELIVERED, new BigDecimal("60.00"));

        when(orderRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class)))
                .thenReturn(List.of(order1, order2));

        var result = dashboardService.getRecentOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getClientName()).isEqualTo("João Silva");
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(result.get(1).getStatus()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("getRecentOrders sem pedidos retorna lista vazia")
    void getRecentOrders_semPedidos_retornaListaVazia() {
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class)))
                .thenReturn(List.of());

        var result = dashboardService.getRecentOrders();

        assertThat(result).isEmpty();
    }

    // --- getTopSellingProducts ---

    @Test
    @DisplayName("getTopSellingProducts retorna até 5 produtos mais vendidos")
    void getTopSellingProducts_retornaListaDTOs() {
        var topProduct = new DashboardTopProductDTO(1L, "Hot Dog Clássico", new BigDecimal("15.90"), 42L);

        when(orderRepository.findTopSellingProducts(any(PageRequest.class)))
                .thenReturn(List.of(topProduct));

        var result = dashboardService.getTopSellingProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hot Dog Clássico");
        assertThat(result.get(0).getTotalSold()).isEqualTo(42L);
    }

    // --- helpers privados ---

    private Client criarClient() {
        var client = new Client();
        client.setId(1L);
        client.setName("João Silva");
        client.setPhone("(11) 91234-5678");
        client.setEmail("joao@email.com");
        client.setCity("São Paulo");
        client.setState(State.SP);
        client.setNeighborhood("Centro");
        client.setStreet("Rua das Flores");
        client.setZipCode("01310-100");
        client.setNumber("10");
        return client;
    }

    private Order criarOrder(Long id, Client client, OrderStatus status, BigDecimal totalIgnored) {
        var order = new Order();
        order.setId(id);
        order.setClient(client);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
