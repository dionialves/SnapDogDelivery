package com.dionialves.snapdogdelivery.domain.storefront.store;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private static final int PAGE_SIZE = 12;

    private final ProductService productService;
    private final CartService cartService;

    /**
     * Landing page pública.
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("featuredProducts", productService.findFeatured());
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/index";
    }

    /**
     * Catálogo público paginado — exibe somente produtos ativos.
     */
    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
        var products = productService.findAllActive(pageable);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/store/catalog";
    }

    /**
     * Detalhe de um produto ativo.
     */
    @GetMapping("/catalog/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        var product = productService.findById(id);

        if (!product.isActive()) {
            throw new NotFoundException("Produto não encontrado com ID: " + id);
        }

        model.addAttribute("product", product);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));
        return "public/store/product-detail";
    }
}
