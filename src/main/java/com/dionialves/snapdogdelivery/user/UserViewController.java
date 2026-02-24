package com.dionialves.snapdogdelivery.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.user.dto.UserCreateDTO;
import com.dionialves.snapdogdelivery.user.dto.UserResponseDTO;
import com.dionialves.snapdogdelivery.user.dto.UserUpdateDTO;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<UserResponseDTO> result = userService.findAll(page);
        model.addAttribute("users", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageTitle", "Usuários");
        model.addAttribute("pageSubtitle", "Gerenciamento de usuários administrativos");
        model.addAttribute("activeMenu", "usuarios");
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new UserCreateDTO());
        model.addAttribute("roles", Role.values());
        model.addAttribute("pageTitle", "Novo Usuário");
        model.addAttribute("pageSubtitle", "Preencha os dados do novo usuário");
        model.addAttribute("activeMenu", "usuarios");
        return "admin/users/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid UserCreateDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("user", dto);
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Novo Usuário");
            model.addAttribute("pageSubtitle", "Preencha os dados do novo usuário");
            model.addAttribute("activeMenu", "usuarios");
            return "admin/users/form";
        }

        try {
            userService.create(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário criado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "sucesso");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "erro");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            UserResponseDTO existing = userService.findById(id);
            var dto = new UserUpdateDTO();
            dto.setName(existing.getName());
            dto.setRole(existing.getRole());

            model.addAttribute("user", dto);
            model.addAttribute("userId", id);
            model.addAttribute("userEmail", existing.getEmail());
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("pageSubtitle", existing.getName());
            model.addAttribute("activeMenu", "usuarios");
            return "admin/users/form";
        } catch (BusinessException e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid UserUpdateDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("user", dto);
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("activeMenu", "usuarios");
            return "admin/users/form";
        }

        try {
            userService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário atualizado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "sucesso");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "erro");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário excluído com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "sucesso");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "erro");
        }
        return "redirect:/admin/users";
    }
}
