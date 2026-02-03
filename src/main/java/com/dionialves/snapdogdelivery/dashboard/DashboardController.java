package com.dionialves.snapdogdelivery.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @GetMapping({ "/", "/dashboard" })
    public String dashboard(Model model) {

        // Por enquanto dados mockados - depois integra com seus Services
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Visão geral do seu negócio");
        model.addAttribute("activeMenu", "dashboard");

        // Mock do usuário logado - depois vem do Spring Security
        model.addAttribute("user", new UserInfo("Dioni", "Administrador"));

        return "admin/dashboard/index";
    }

    // Inner class temporária pro usuário - depois usa sua entidade real
    public record UserInfo(String name, String role) {
    }
}
