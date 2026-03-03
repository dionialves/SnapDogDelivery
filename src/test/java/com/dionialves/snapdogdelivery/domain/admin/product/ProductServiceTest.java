package com.dionialves.snapdogdelivery.domain.admin.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dionialves.snapdogdelivery.domain.admin.product.Product;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.productorder.ProductOrderRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Hot Dog Clássico");
        product.setPrice(new BigDecimal("15.90"));
        product.setDescription("Pão, salsicha, mostarda e ketchup");

        productDTO = new ProductDTO();
        productDTO.setName("Hot Dog Clássico");
        productDTO.setPrice(new BigDecimal("15.90"));
        productDTO.setDescription("Pão, salsicha, mostarda e ketchup");
    }

    // --- findFeatured ---

    @Test
    @DisplayName("findFeatured com menos de 6 destaques complementa com produtos ativos")
    void findFeatured_menosDe6Featured_complementaComAtivos() {
        var featuredProduct = new Product();
        featuredProduct.setId(1L);
        featuredProduct.setName("Hot Dog Destaque");
        featuredProduct.setPrice(new BigDecimal("15.90"));
        featuredProduct.setFeatured(true);
        featuredProduct.setActive(true);

        var activeProduct = new Product();
        activeProduct.setId(2L);
        activeProduct.setName("Hot Dog Comum");
        activeProduct.setPrice(new BigDecimal("12.90"));
        activeProduct.setActive(true);

        when(productRepository.findByActiveTrueAndFeaturedTrueOrderByNameAsc())
                .thenReturn(List.of(featuredProduct));
        when(productRepository.findByActiveTrueAndFeaturedFalseOrderByNameAsc(any()))
                .thenReturn(new PageImpl<>(List.of(activeProduct)));

        var result = productService.findFeatured();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Hot Dog Destaque");
        assertThat(result.get(1).getName()).isEqualTo("Hot Dog Comum");
    }

    // --- create com featured ---

    @Test
    @DisplayName("create com featured=true e limite atingido lança BusinessException")
    void create_comFeatured_limiteAtingido_lancaBusinessException() {
        productDTO.setFeatured(true);
        when(productRepository.countByFeaturedTrue()).thenReturn(6L);

        assertThatThrownBy(() -> productService.create(productDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("destaque");

        verify(productRepository, never()).save(any());
    }

    // --- update com featured ---

    @Test
    @DisplayName("update marcando featured=true com limite atingido lança BusinessException")
    void update_marcaFeatured_limiteAtingido_lancaBusinessException() {
        product.setFeatured(false);
        productDTO.setFeatured(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.countByFeaturedTrue()).thenReturn(6L);

        assertThatThrownBy(() -> productService.update(1L, productDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("destaque");
    }

    // --- search (lista) ---

    @Test
    @DisplayName("search sem paginação retorna lista de DTOs")
    void search_semPaginacao_retornaListaDTOs() {
        when(productRepository.findByNameContainingIgnoreCase("hot")).thenReturn(List.of(product));

        var result = productService.search("hot");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hot Dog Clássico");
    }

    @Test
    @DisplayName("search paginado retorna Page de DTOs")
    void search_paginado_retornaPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductResponseDTO> result = productService.search("hot", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPrice()).isEqualByComparingTo("15.90");
    }

    // --- findById ---

    @Test
    @DisplayName("findById com ID existente retorna DTO")
    void findById_existente_retornaDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hot Dog Clássico");
    }

    @Test
    @DisplayName("findById com ID inexistente lança NotFoundException")
    void findById_inexistente_lancaNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    @DisplayName("create persiste e retorna DTO do produto criado")
    void create_dadosValidos_persisteERetornaDTO() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var result = productService.create(productDTO);

        assertThat(result.getName()).isEqualTo("Hot Dog Clássico");
        assertThat(result.getPrice()).isEqualByComparingTo("15.90");
        verify(productRepository).save(any(Product.class));
    }

    // --- update ---

    @Test
    @DisplayName("update com ID existente atualiza e retorna DTO")
    void update_existente_atualizaERetornaDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productDTO.setName("Hot Dog Especial");
        productDTO.setPrice(new BigDecimal("19.90"));

        var result = productService.update(1L, productDTO);

        assertThat(result.getName()).isEqualTo("Hot Dog Especial");
        assertThat(result.getPrice()).isEqualByComparingTo("19.90");
    }

    @Test
    @DisplayName("update com ID inexistente lança NotFoundException")
    void update_inexistente_lancaNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, productDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    @DisplayName("delete com ID existente e sem pedidos remove o produto")
    void delete_existente_removeSucesso() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productOrderRepository.existsByProductId(1L)).thenReturn(false);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete com ID inexistente lança NotFoundException")
    void delete_inexistente_lancaNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete com pedidos associados lança BusinessException")
    void delete_comPedidosAssociados_lancaBusinessException() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productOrderRepository.existsByProductId(1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pedidos");

        verify(productRepository, never()).deleteById(any());
    }
}
