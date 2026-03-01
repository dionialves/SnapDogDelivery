package com.dionialves.snapdogdelivery.domain.admin.settings;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.domain.admin.settings.dto.CompanySettingsDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/settings")
public class CompanySettingsViewController {

    private final CompanySettingsService settingsService;

    @GetMapping
    public String show(Model model) {
        CompanySettings settings = settingsService.get();
        model.addAttribute("settings", CompanySettingsDTO.fromEntity(settings));
        model.addAttribute("activeMenu", "configuracoes");
        model.addAttribute("pageTitle", "Configurações da Empresa");
        return "admin/settings/index";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("settings") CompanySettingsDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "configuracoes");
            model.addAttribute("pageTitle", "Configurações da Empresa");
            return "admin/settings/index";
        }

        try {
            settingsService.update(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Configurações salvas com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar configurações: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }
}
