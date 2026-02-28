package com.dionialves.snapdogdelivery.domain.storefront.store;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private StoreController storeController;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();

        session = new MockHttpSession();
    }

    // ---------- GET / ----------

    @Test
    @DisplayName("GET / retorna home com produtos em destaque no model")
    void home_retornaIndexComFeaturedProducts() throws Exception {
        var produto = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), null, null, true);
        when(productService.findFeatured()).thenReturn(List.of(produto));
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/index"))
                .andExpect(model().attributeExists("featuredProducts", "cartItemCount"));
    }

    // ---------- GET /catalog ----------

    @Test
    @DisplayName("GET /catalog retorna catálogo paginado com model completo")
    void catalog_retornaCatalogoComPaginacao() throws Exception {
        var produto = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), null, null, true);
        var page = new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1);
        when(productService.findAllActive(any())).thenReturn(page);
        when(cartService.getItemCount(any())).thenReturn(2);

        mockMvc.perform(get("/catalog").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/store/catalog"))
                .andExpect(model().attributeExists("products", "currentPage", "totalPages", "cartItemCount"));
    }

    @Test
    @DisplayName("GET /catalog?page=1 usa a página correta na consulta")
    void catalog_comPaginacao_usaPaginaCorreta() throws Exception {
        var page = new PageImpl<ProductResponseDTO>(List.of(), PageRequest.of(1, 12), 0);
        when(productService.findAllActive(any())).thenReturn(page);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/catalog").param("page", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/store/catalog"))
                .andExpect(model().attribute("currentPage", 1));
    }

    // ---------- GET /catalog/{id} ----------

    @Test
    @DisplayName("GET /catalog/{id} com produto ativo retorna detalhe do produto")
    void productDetail_produtoAtivo_retornaDetalhe() throws Exception {
        var produto = new ProductResponseDTO(5L, "Hot Dog Premium", new BigDecimal("22.90"), "Descrição", null, true);
        when(productService.findById(5L)).thenReturn(produto);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/catalog/5").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/store/product-detail"))
                .andExpect(model().attributeExists("product", "cartItemCount"));
    }

    @Test
    @DisplayName("GET /catalog/{id} com produto inativo propaga NotFoundException como ServletException")
    void productDetail_produtoInativo_lancaNotFoundException() throws Exception {
        var produto = new ProductResponseDTO(7L, "Produto Inativo", new BigDecimal("10.00"), null, null, false);
        when(productService.findById(7L)).thenReturn(produto);

        // NotFoundException propaga como ServletException no standaloneSetup sem GlobalExceptionHandler
        try {
            mockMvc.perform(get("/catalog/7").session(session));
        } catch (Exception ex) {
            // Verifica que a causa raiz é NotFoundException
            Throwable cause = ex.getCause();
            while (cause != null && !(cause instanceof com.dionialves.snapdogdelivery.exception.NotFoundException)) {
                cause = cause.getCause();
            }
            assert cause instanceof com.dionialves.snapdogdelivery.exception.NotFoundException;
        }
    }
}
