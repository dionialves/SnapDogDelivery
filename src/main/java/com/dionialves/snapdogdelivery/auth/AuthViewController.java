package com.dionialves.snapdogdelivery.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "E-mail ou senha inválidos.");
        }

        if (logout != null) {
            model.addAttribute("message", "Você saiu com sucesso.");
        }

        return "auth/login";
    }
}