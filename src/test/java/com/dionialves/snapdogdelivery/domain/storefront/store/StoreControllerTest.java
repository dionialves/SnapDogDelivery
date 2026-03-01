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

import com.dionialves.snapdogdelivery.domain.admin.product.ProductCategory;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;

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
        var produto = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), null, null, true, null, null);
        when(productService.findFeatured()).thenReturn(List.of(produto));
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/index"))
                .andExpect(model().attributeExists("featuredProducts", "cartItemCount"));
    }

    // ---------- GET /catalog ----------

    @Test
    @DisplayName("GET /catalog sem categoria retorna view 'Todos' com duas seções")
    void catalog_semCategoria_retornaDuasSecoes() throws Exception {
        var hotDog = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), null, null, true, ProductCategory.HOT_DOG, "Hot Dog");
        var bebida = new ProductResponseDTO(2L, "Suco", new BigDecimal("8.00"), null, null, true, ProductCategory.BEBIDA, "Bebida");
        when(productService.findAllActiveByCategory(ProductCategory.HOT_DOG)).thenReturn(List.of(hotDog));
        when(productService.findAllActiveByCategory(ProductCategory.BEBIDA)).thenReturn(List.of(bebida));
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/catalog").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/store/catalog"))
                .andExpect(model().attributeExists("hotDogProducts", "bebidaProducts", "cartItemCount"));
    }

    @Test
    @DisplayName("GET /catalog?category=HOT_DOG retorna grid paginado filtrado")
    void catalog_comCategoria_retornaPaginadoFiltrado() throws Exception {
        var produto = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), null, null, true, ProductCategory.HOT_DOG, "Hot Dog");
        var page = new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1);
        when(productService.findAllActive(any(), any())).thenReturn(page);
        when(cartService.getItemCount(any())).thenReturn(2);

        mockMvc.perform(get("/catalog").param("category", "HOT_DOG").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/store/catalog"))
                .andExpect(model().attributeExists("products", "currentPage", "totalPages", "cartItemCount"));
    }

}
