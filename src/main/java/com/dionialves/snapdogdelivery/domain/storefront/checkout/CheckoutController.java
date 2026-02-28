package com.dionialves.snapdogdelivery.domain.storefront.checkout;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderService;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controlador do fluxo de checkout.
 * Todas as rotas exigem autenticação com role CUSTOMER (configurado no SecurityConfig).
 * O principal autenticado é um Customer (via CustomerUserDetailsService).
 */
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    /**
     * Tela de revisão — exibe o resumo do carrinho e o endereço de entrega.
     */
    @GetMapping
    public String reviewPage(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        var cart = cartService.getCart(session);

        if (cart.isEmpty()) {
            return "redirect:/catalog";
        }

        var customer = loadCustomer(userDetails);
        model.addAttribute("cart", cart);
        model.addAttribute("customer", customer);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/checkout/review";
    }

    /**
     * Confirma o pedido — converte o carrinho em Order e redireciona para a tela de sucesso.
     */
    @PostMapping("/confirm")
    public String confirm(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            var customer = loadCustomer(userDetails);
            var order = checkoutService.createOrderFromCart(session, customer);
            return "redirect:/checkout/confirmation/" + order.getId();

        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Tela de confirmação de pedido — exibida após o checkout bem-sucedido.
     */
    @GetMapping("/confirmation/{orderId}")
    public String confirmation(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            HttpSession session) {

        var order = orderService.findById(orderId);

        // Garante que o pedido pertence ao cliente autenticado
        var customer = loadCustomer(userDetails);
        if (!order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/account";
        }

        model.addAttribute("order", order);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/checkout/confirmation";
    }

    private Customer loadCustomer(UserDetails userDetails) {
        return customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado."));
    }
}
