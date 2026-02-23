package com.dionialves.snapdogdelivery.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/clients")
public class ClientViewController {

    private final ClientService clientService;

    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String findAll(Model model,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        Page<ClientDTO> clientPage = clientService.search(search, page, PAGE_SIZE);
        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", clientPage.getNumber());
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalElements", clientPage.getTotalElements());
        model.addAttribute("search", search);
        return "admin/clients/list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {

        var client = clientService.findById(id);
        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", client.getName());
        model.addAttribute("pageSubtitle", "Dados cadastrais do cliente");

        model.addAttribute("client", client);
        return "admin/clients/form";
    }

    @GetMapping("/new")
    public String newClient(Model model) {

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Novo Cliente");
        model.addAttribute("pageSubtitle", "Adicione um novo cliente a sua base");

        model.addAttribute("client", new ClientDTO());

        return "admin/clients/form";
    }

    @PostMapping("/new")
    public String saved(
            @Valid @ModelAttribute("client") ClientDTO client,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "clientes");
            model.addAttribute("pageTitle", "Novo Cliente");
            model.addAttribute("pageSubtitle", "Adicione um novo cliente a sua base");

            return "admin/clients/form";
        }

        try {
            clientService.create(client);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente criado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("client") ClientDTO client,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            model.addAttribute("activeMenu", "clientes");
            model.addAttribute("pageTitle", client.getName());
            model.addAttribute("pageSubtitle", "Dados cadastrais do client");

            return "admin/clients/form";
        }

        try {
            clientService.update(id, client);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente atualizado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        try {
            clientService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente deletado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/clients";
    }
}
