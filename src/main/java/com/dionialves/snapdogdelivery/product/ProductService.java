package com.dionialves.snapdogdelivery.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.product.dto.ProductDTO;
import com.dionialves.snapdogdelivery.product.dto.ProductResponseDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    public final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> search(String search) {

        return productRepository.findByNameContainingIgnoreCase(search)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with ID: " + id));

        return ProductResponseDTO.fromEntity(product);

    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByName(String name) {

        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();

    }

    public ProductResponseDTO create(ProductDTO product) {

        Product created = new Product();
        created.setName(product.getName());
        created.setPrice(product.getPrice());
        created.setDescription(product.getDescription());

        productRepository.save(created);

        return ProductResponseDTO.fromEntity(created);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductDTO product) {

        Product updating = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        updating.setName(product.getName());
        updating.setPrice(product.getPrice());
        updating.setDescription(product.getDescription());

        return ProductResponseDTO.fromEntity(updating);

    }

    public void delete(Long id) {
        if (!productRepository.existsById(id))
            throw new NotFoundException("Product not found with ID: " + id);

        productRepository.deleteById(id);
    }

}
