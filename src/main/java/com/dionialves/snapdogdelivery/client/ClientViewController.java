package com.dionialves.snapdogdelivery.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/clients")
public class ClientViewController {

    private final ClientService clientService;

    @GetMapping
    public String findAll(Model model) {

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        List<ClientDTO> clients = clientService.findAll();
        model.addAttribute("clients", clients);

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

        clientService.create(client);

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

        clientService.update(id, client);

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Model model) {

        clientService.delete(id);

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        return "redirect:/admin/clients";
    }
}
