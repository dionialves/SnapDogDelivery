package com.dionialves.snapdogdelivery.domain.storefront.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controlador do carrinho de compras.
 * Todas as rotas exigem autenticação com role CUSTOMER (configurado no SecurityConfig).
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Exibe o carrinho atual.
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        var cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/cart/cart";
    }

    /**
     * Adiciona um produto ao carrinho e redireciona de volta ao catálogo.
     */
    @PostMapping("/add")
    public String addItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.addItem(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Produto adicionado ao carrinho!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/catalog";
    }

    /**
     * Remove um item do carrinho.
     */
    @PostMapping("/remove/{productId}")
    public String removeItem(
            @PathVariable Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        cartService.removeItem(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removido do carrinho.");
        return "redirect:/cart";
    }

    /**
     * Atualiza a quantidade de um item do carrinho.
     */
    @PostMapping("/update/{productId}")
    public String updateItem(
            @PathVariable Long productId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    /**
     * Esvazia o carrinho.
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clear(session);
        redirectAttributes.addFlashAttribute("successMessage", "Carrinho esvaziado.");
        return "redirect:/cart";
    }
}
