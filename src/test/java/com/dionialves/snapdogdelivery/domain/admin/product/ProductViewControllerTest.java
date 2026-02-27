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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.infra.storage.StorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ProductViewControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProductViewController productViewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(productViewController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();
    }

    // ---------- GET /admin/products ----------

    @Test
    @DisplayName("GET /admin/products retorna lista paginada com model completo")
    void findAll_retornaListaComPaginacao() throws Exception {
        var dto = buildProductResponseDTO(1L, "Hot Dog Clássico");
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(productService.search(any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products/list"))
                .andExpect(model().attributeExists("products", "currentPage", "totalPages", "totalElements"));
    }

    // ---------- GET /admin/products/new ----------

    @Test
    @DisplayName("GET /admin/products/new retorna formulário com ProductDTO vazio")
    void newProduct_retornaFormularioVazio() throws Exception {
        mockMvc.perform(get("/admin/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products/form"))
                .andExpect(model().attributeExists("product"));
    }

    // ---------- GET /admin/products/{id} ----------

    @Test
    @DisplayName("GET /admin/products/{id} retorna formulário com dados do produto")
    void findById_retornaFormularioPreenchido() throws Exception {
        var dto = buildProductResponseDTO(5L, "Hot Dog Premium");
        when(productService.findById(5L)).thenReturn(dto);

        mockMvc.perform(get("/admin/products/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products/form"))
                .andExpect(model().attribute("product", dto));
    }

    // ---------- POST /admin/products/new com URL de imagem ----------

    @Test
    @DisplayName("POST /admin/products/new com URL de imagem cria produto e redireciona com sucesso")
    void saved_comUrlImagem_redirecionaComSucesso() throws Exception {
        when(productService.create(any())).thenReturn(buildProductResponseDTO(10L, "Hot Dog"));

        mockMvc.perform(post("/admin/products/new")
                .param("name", "Hot Dog")
                .param("price", "15.90")
                .param("imageUrl", "https://exemplo.com/imagem.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attribute("successMessage", "Produto salvo com sucesso!"));

        // StorageService NÃO deve ser chamado quando apenas URL é fornecida
        verify(storageService, never()).store(any());
    }

    // ---------- POST /admin/products/new com arquivo de imagem ----------

    @Test
    @DisplayName("POST /admin/products/new com arquivo de imagem chama StorageService e redireciona")
    void saved_comArquivoImagem_chamaStorageERedirecionaComSucesso() throws Exception {
        var imageFile = new MockMultipartFile(
                "imageFile", "logo.jpg", "image/jpeg", "conteudo".getBytes());

        when(storageService.store(any())).thenReturn("/uploads/products/uuid.jpg");
        when(productService.create(any())).thenReturn(buildProductResponseDTO(11L, "Hot Dog"));

        mockMvc.perform(multipart("/admin/products/new")
                .file(imageFile)
                .param("name", "Hot Dog")
                .param("price", "15.90"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attribute("successMessage", "Produto salvo com sucesso!"));

        verify(storageService).store(any());
    }

    // ---------- POST /admin/products/{id} ----------

    @Test
    @DisplayName("POST /admin/products/{id} atualiza produto e redireciona com sucesso")
    void update_dadosValidos_redirecionaComSucesso() throws Exception {
        when(productService.update(eq(1L), any())).thenReturn(buildProductResponseDTO(1L, "Hot Dog Atualizado"));

        mockMvc.perform(post("/admin/products/1")
                .param("name", "Hot Dog Atualizado")
                .param("price", "18.90"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attribute("successMessage", "Produto atualizado com sucesso!"));
    }

    // ---------- POST /admin/products/{id}/delete ----------

    @Test
    @DisplayName("POST /admin/products/{id}/delete exclui produto e redireciona com sucesso")
    void delete_produtoExistente_redirecionaComSucesso() throws Exception {
        doNothing().when(productService).delete(3L);

        mockMvc.perform(post("/admin/products/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attribute("successMessage", "Produto deletado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/products/{id}/delete com BusinessException redireciona com errorMessage")
    void delete_businessException_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Produto associado a pedidos"))
                .when(productService).delete(99L);

        mockMvc.perform(post("/admin/products/99/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- Auxiliares ----------

    private ProductResponseDTO buildProductResponseDTO(Long id, String name) {
        return new ProductResponseDTO(id, name, new BigDecimal("15.90"), "Descrição", null, true);
    }
}
