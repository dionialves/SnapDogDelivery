package com.dionialves.snapdogdelivery.domain.admin.customer;

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

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/customers")
public class CustomerViewController {

    private final CustomerService customerService;

    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String findAll(Model model,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "customers");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        Page<CustomerDTO> customerPage = customerService.search(search, page, PAGE_SIZE);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", customerPage.getNumber());
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalElements", customerPage.getTotalElements());
        model.addAttribute("search", search);
        return "admin/customers/list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {

        var customer = customerService.findById(id);
        model.addAttribute("activeMenu", "customers");
        model.addAttribute("pageTitle", customer.getName());
        model.addAttribute("pageSubtitle", "Dados cadastrais do cliente");

        model.addAttribute("customer", customer);
        return "admin/customers/form";
    }

    @GetMapping("/new")
    public String newCustomer(Model model) {

        model.addAttribute("activeMenu", "customers");
        model.addAttribute("pageTitle", "Novo Cliente");
        model.addAttribute("pageSubtitle", "Adicione um novo cliente a sua base");

        model.addAttribute("customer", new CustomerDTO());

        return "admin/customers/form";
    }

    @PostMapping("/new")
    public String saved(
            @Valid @ModelAttribute("customer") CustomerDTO customer,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "customers");
            model.addAttribute("pageTitle", "Novo Cliente");
            model.addAttribute("pageSubtitle", "Adicione um novo cliente a sua base");

            return "admin/customers/form";
        }

        try {
            customerService.create(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente criado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("customer") CustomerDTO customer,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            model.addAttribute("activeMenu", "customers");
            model.addAttribute("pageTitle", customer.getName());
            model.addAttribute("pageSubtitle", "Dados cadastrais do cliente");

            return "admin/customers/form";
        }

        try {
            customerService.update(id, customer);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente atualizado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        try {
            customerService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente deletado com sucesso!");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/customers";
    }
}
