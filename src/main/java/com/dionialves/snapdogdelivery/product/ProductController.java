package com.dionialves.snapdogdelivery.product;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dionialves.snapdogdelivery.product.dto.ProductDTO;
import com.dionialves.snapdogdelivery.product.dto.ProductResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {

        List<ProductResponseDTO> products = productService.findAll();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {

        ProductResponseDTO product = productService.findById(id);
        return ResponseEntity.ok(product);

    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> newItem(@Valid @RequestBody ProductDTO product) {

        ProductResponseDTO created = productService.create(product);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);

    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@Valid @RequestBody ProductDTO product, @PathVariable Long id) {

        ProductResponseDTO updating = productService.update(id, product);
        return ResponseEntity.ok(updating);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        productService.delete(id);
        return ResponseEntity.noContent().build();

    }
}
