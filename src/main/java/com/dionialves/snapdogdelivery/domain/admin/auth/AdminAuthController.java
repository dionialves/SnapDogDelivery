package com.dionialves.snapdogdelivery.domain.admin.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsável por servir a tela de login da área administrativa.
 * O processamento do formulário (POST /admin/login) é responsabilidade do Spring Security.
 */
@Controller
public class AdminAuthController {

    /**
     * Exibe o formulário de login do painel administrativo.
     */
    @GetMapping("/admin/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "E-mail ou senha inválidos.");
        }

        if (logout != null) {
            model.addAttribute("message", "Você saiu com sucesso.");
        }

        return "admin/auth/login";
    }
}
