package com.dionialves.snapdogdelivery.domain.storefront.store;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dionialves.snapdogdelivery.domain.admin.product.ProductCategory;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;

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
     * Catálogo público — sem filtro: duas seções (Hot Dog e Bebidas, sem paginação).
     * Com filtro por categoria: grid paginado.
     */
    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) ProductCategory category,
            Model model,
            HttpSession session) {

        model.addAttribute("selectedCategory", category);
        model.addAttribute("cartItemCount", cartService.getItemCount(session));

        if (category == null) {
            model.addAttribute("hotDogProducts", productService.findAllActiveByCategory(ProductCategory.HOT_DOG));
            model.addAttribute("bebidaProducts", productService.findAllActiveByCategory(ProductCategory.BEBIDA));
        } else {
            var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
            var products = productService.findAllActive(pageable, category);
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
        }

        return "public/store/catalog";
    }

}
