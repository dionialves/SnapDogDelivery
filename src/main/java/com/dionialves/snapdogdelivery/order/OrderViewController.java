package com.dionialves.snapdogdelivery.order;

import java.util.ArrayList;
import java.util.List;

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

import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/orders")
public class OrderViewController {

    private final OrderService orderService;

    @GetMapping
    public String findAll(Model model,
            @RequestParam(required = false, defaultValue = "") OrderStatus status,
            @RequestParam(required = false, defaultValue = "") String q) {

        model.addAttribute("activeMenu", "Pedidos");
        model.addAttribute("pageTitle", "Pedidos");
        model.addAttribute("pageSubtitle", "Gerencie os pedidos cadastrados");

        List<OrderResponseDTO> orders = orderService.search(status, q);

        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", status != null ? status.name() : null);
        model.addAttribute("search", q);

        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {

        var order = orderService.findById(id);
        model.addAttribute("activeMenu", "Pedidos");
        model.addAttribute("pageTitle", "Pedido #" + order.getId());
        model.addAttribute("pageSubtitle", "Detalhes do pedido");

        model.addAttribute("order", order);
        return "admin/orders/form";
    }

    @GetMapping("/new")
    public String newOrder(Model model) {

        model.addAttribute("activeMenu", "Pedidos");
        model.addAttribute("pageTitle", "Novo Pedido");
        model.addAttribute("pageSubtitle", "Adicione um novo pedido a sua base");

        model.addAttribute("order", new OrderCreateDTO());

        List<ProductOrderDTO> products = new ArrayList<>();
        model.addAttribute("products", products);

        return "admin/orders/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("order") OrderCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "Pedidos");
            model.addAttribute("pageTitle", "Novo Pedido");
            model.addAttribute("pageSubtitle", "Adicione um novo pedido a sua base");

            return "admin/orders/form";
        }
        try {
            orderService.create(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido criado com sucesso!");

            return "redirect:/admin/orders";

        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/admin/orders/new";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("order") OrderCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "Pedidos");
            model.addAttribute("pageTitle", "Editar Pedido");
            model.addAttribute("pageSubtitle", "Edite os dados do pedido");

            return "admin/orders/form";
        }

        try {
            orderService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido atualizado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status);
            orderService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Status atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Status inválido: " + status);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            orderService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido deletado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders";
    }
}
