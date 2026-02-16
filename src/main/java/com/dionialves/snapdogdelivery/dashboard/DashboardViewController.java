package com.dionialves.snapdogdelivery.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dionialves.snapdogdelivery.dashboard.dto.DashboardSummaryDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class DashboardViewController {

    private final DashboardService dashboardService;

    @GetMapping({ "/", "/dashboard" })
    public String dashboard(Model model) {

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Visão geral do seu negócio");
        model.addAttribute("activeMenu", "dashboard");

        model.addAttribute("dashboardSummary", dashboardService.getDashboardSummary());
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts());

        return "admin/dashboard/index";
    }
}
