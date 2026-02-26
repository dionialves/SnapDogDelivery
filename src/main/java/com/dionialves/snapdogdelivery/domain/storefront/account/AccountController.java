package com.dionialves.snapdogdelivery.domain.storefront.account;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerService;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderService;
import com.dionialves.snapdogdelivery.domain.admin.user.User;
import com.dionialves.snapdogdelivery.domain.admin.user.UserRepository;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador da área do cliente (/account/**).
 * Todas as rotas exigem autenticação com role CUSTOMER (configurado no SecurityConfig).
 */
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private static final int PAGE_SIZE = 10;

    private final OrderService orderService;
    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final CartService cartService;

    /**
     * Painel principal da conta — exibe os 5 pedidos mais recentes.
     */
    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        var user = loadUser(userDetails);
        var recentOrders = orderService.findRecentByCustomerId(user.getCustomer().getId());

        model.addAttribute("customer", user.getCustomer());
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/account/dashboard";
    }

    /**
     * Histórico completo de pedidos paginado.
     */
    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        var user = loadUser(userDetails);
        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        var orders = orderService.findByCustomerId(user.getCustomer().getId(), pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/account/orders";
    }

    /**
     * Detalhe de um pedido específico do cliente autenticado.
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        var user = loadUser(userDetails);
        var order = orderService.findById(id);

        // Verifica se o pedido pertence ao cliente autenticado
        if (!order.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new NotFoundException("Pedido não encontrado.");
        }

        model.addAttribute("order", order);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/account/order-detail";
    }

    /**
     * Formulário de edição de perfil.
     */
    @GetMapping("/profile")
    public String profileForm(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        var user = loadUser(userDetails);
        var customerDTO = customerService.findById(user.getCustomer().getId());

        model.addAttribute("customerDTO", customerDTO);
        model.addAttribute("states", State.values());
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/account/profile";
    }

    /**
     * Salva as alterações do perfil do cliente.
     */
    @PostMapping("/profile")
    public String updateProfile(
            @Valid CustomerDTO customerDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("states", State.values());
            model.addAttribute("cartItemCount", cartService.getItemCount(session));
            return "public/account/profile";
        }

        try {
            var user = loadUser(userDetails);
            customerService.update(user.getCustomer().getId(), customerDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/account/profile";
    }

    private User loadUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
    }
}
