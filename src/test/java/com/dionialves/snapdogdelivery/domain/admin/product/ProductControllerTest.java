package com.dionialves.snapdogdelivery.domain.admin.product;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dionialves.snapdogdelivery.domain.admin.product.ProductController;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    @DisplayName("GET /admin/api/products/search retorna lista de produtos")
    void search_retornaListaProdutos() throws Exception {
        var dto = new ProductResponseDTO(1L, "Hot Dog Clássico", new BigDecimal("15.90"),
                "Pão, salsicha, mostarda", null, true);

        when(productService.search("hot")).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/api/products/search").param("q", "hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hot Dog Clássico"))
                .andExpect(jsonPath("$[0].price").value(15.90));
    }

    @Test
    @DisplayName("GET /admin/api/products/search com termo vazio retorna todos")
    void search_termoVazio_retornaTodos() throws Exception {
        var dto1 = new ProductResponseDTO(1L, "Hot Dog Clássico", new BigDecimal("15.90"), null, null, true);
        var dto2 = new ProductResponseDTO(2L, "X-Burguer", new BigDecimal("22.50"), null, null, true);

        when(productService.search("")).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/admin/api/products/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /admin/api/products/search sem resultados retorna lista vazia")
    void search_semResultados_retornaListaVazia() throws Exception {
        when(productService.search("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/products/search").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
