package com.dionialves.snapdogdelivery.domain.admin.dashboard;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.dashboard.dto.DashboardSummaryDTO;
import com.dionialves.snapdogdelivery.domain.admin.dashboard.dto.DashboardTopProductDTO;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class DashboardViewControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardViewController dashboardViewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardViewController)
                .setViewResolvers(viewResolver)
                .build();
    }

    // ---------- GET /admin/dashboard ----------

    @Test
    @DisplayName("GET /admin/dashboard retorna view correta com todos os atributos do model")
    void dashboard_retornaViewComModel() throws Exception {
        var summary = buildSummary();
        var topProduct = new DashboardTopProductDTO(1L, "Hot Dog Clássico", new BigDecimal("15.90"), 42L);

        when(dashboardService.getDashboardSummary()).thenReturn(summary);
        when(dashboardService.getRecentOrders()).thenReturn(List.of());
        when(dashboardService.getTopSellingProducts()).thenReturn(List.of(topProduct));

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard/index"))
                .andExpect(model().attributeExists("dashboardSummary", "recentOrders", "topProducts"))
                .andExpect(model().attribute("pageTitle", "Dashboard"))
                .andExpect(model().attribute("activeMenu", "dashboard"));
    }

    // ---------- GET /admin/ ----------

    @Test
    @DisplayName("GET /admin/ retorna a mesma view que /admin/dashboard")
    void adminRoot_retornaMesmaViewQueDashboard() throws Exception {
        when(dashboardService.getDashboardSummary()).thenReturn(buildSummary());
        when(dashboardService.getRecentOrders()).thenReturn(List.of());
        when(dashboardService.getTopSellingProducts()).thenReturn(List.of());

        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard/index"))
                .andExpect(model().attributeExists("dashboardSummary"));
    }

    // ---------- Auxiliares ----------

    private DashboardSummaryDTO buildSummary() {
        var summary = new DashboardSummaryDTO();
        summary.setOrdersToday(5L);
        summary.setOrdersYesterday(3L);
        summary.setRevenueToday(new BigDecimal("150.00"));
        summary.setRevenueYesterday(new BigDecimal("90.00"));
        summary.setNewCustomersToday(2L);
        summary.setNewCustomersYesterday(1L);
        summary.setAverageTicketToday(new BigDecimal("30.00"));
        summary.setAverageTicketYesterday(new BigDecimal("30.00"));
        return summary;
    }
}
