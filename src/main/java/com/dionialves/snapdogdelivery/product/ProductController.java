package com.dionialves.snapdogdelivery.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dionialves.snapdogdelivery.product.dto.ProductResponseDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(@RequestParam(name = "q", defaultValue = "") String q) {

        List<ProductResponseDTO> products = productService.search(q);

        return ResponseEntity.ok(products);
    }

}
