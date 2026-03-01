package com.dionialves.snapdogdelivery.domain.admin.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.productorder.ProductOrderRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> search(String search) {

        return productRepository.findByNameContainingIgnoreCase(search)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> search(String search, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        return productRepository.findByNameContainingIgnoreCase(search, pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Produto não encontrado com ID: " + id));

        return ProductResponseDTO.fromEntity(product);

    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByName(String name) {

        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();

    }

    /**
     * Retorna produtos ativos paginados para exibição no catálogo público.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    /**
     * Retorna até 6 produtos ativos para a seção de destaques da landing page.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findFeatured() {
        return productRepository.findTop6ByActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public ProductResponseDTO create(ProductDTO product) {

        Product created = new Product();
        created.setName(product.getName());
        created.setPrice(product.getPrice());
        created.setDescription(product.getDescription());
        created.setImageUrl(product.getImageUrl());
        created.setActive(product.isActive());

        productRepository.save(created);

        return ProductResponseDTO.fromEntity(created);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductDTO product) {

        Product updating = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado com ID: " + id));

        updating.setName(product.getName());
        updating.setPrice(product.getPrice());
        updating.setDescription(product.getDescription());
        updating.setImageUrl(product.getImageUrl());
        updating.setActive(product.isActive());

        return ProductResponseDTO.fromEntity(updating);

    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id))
            throw new NotFoundException("Produto não encontrado com ID: " + id);

        if (productOrderRepository.existsByProductId(id))
            throw new BusinessException("Produto não pode ser excluído pois está associado a pedidos existentes.");

        productRepository.deleteById(id);
    }

}
