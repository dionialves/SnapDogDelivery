package com.dionialves.snapdogdelivery.domain.admin.product;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.infra.storage.StorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/products")
public class ProductViewController {

    private final ProductService productService;
    private final StorageService storageService;

    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String findAll(Model model,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "produtos");
        model.addAttribute("pageTitle", "Produtos");
        model.addAttribute("pageSubtitle", "Gerencie os produtos cadastrados");

        Page<ProductResponseDTO> productPage = productService.search(search, page, PAGE_SIZE);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("search", search);

        return "admin/products/list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {

        model.addAttribute("activeMenu", "produtos");
        model.addAttribute("pageTitle", "Produto");
        model.addAttribute("pageSubtitle", "Detalhes do produto");

        ProductResponseDTO product = productService.findById(id);
        model.addAttribute("product", product);

        return "admin/products/form";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {

        model.addAttribute("activeMenu", "produtos");
        model.addAttribute("pageTitle", "Novo Produto");
        model.addAttribute("pageSubtitle", "Adicione um novo produto a sua base");

        model.addAttribute("product", new ProductDTO());

        return "admin/products/form";
    }

    @PostMapping("/new")
    public String saved(
            @Valid @ModelAttribute("product") ProductDTO product,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "produtos");
            model.addAttribute("pageTitle", "Novo Produto");
            model.addAttribute("pageSubtitle", "Adicione um novo produto a sua base");

            return "admin/products/form";
        }

        try {
            resolveImageUrl(product, imageFile);
            productService.create(product);
            redirectAttributes.addFlashAttribute("successMessage", "Produto salvo com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar o produto: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDTO product,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "produtos");
            model.addAttribute("pageTitle", "Editar Produto");
            model.addAttribute("pageSubtitle", "Atualize os dados do produto");

            return "admin/products/form";
        }

        try {
            resolveImageUrl(product, imageFile);
            productService.update(id, product);
            redirectAttributes.addFlashAttribute("successMessage", "Produto atualizado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar o produto: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    /**
     * Prioriza o arquivo enviado sobre a URL externa.
     * Se um arquivo for enviado, faz upload e sobrescreve imageUrl no DTO.
     */
    private void resolveImageUrl(ProductDTO product, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(storageService.store(imageFile));
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto deletado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar o produto: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }
}
