package com.dionialves.snapdogdelivery.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class OrderViewControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderViewController orderViewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Evita erro de "circular view path" ao resolver nomes de view sem container real
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(orderViewController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();
    }

    // ---------- GET /admin/orders ----------

    @Test
    @DisplayName("GET /admin/orders retorna lista de pedidos no modelo")
    void findAll_retornaListaPedidos() throws Exception {
        var dto = orderResponseDTO(1L, "PENDING");
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(orderService.search(any(), any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/list"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages"));
    }

    @Test
    @DisplayName("GET /admin/orders com filtro de status retorna pedidos filtrados")
    void findAll_comFiltroStatus_retornaPedidosFiltrados() throws Exception {
        var dto = orderResponseDTO(2L, "PREPARING");
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(orderService.search(eq(OrderStatus.PREPARING), any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/admin/orders")
                        .param("status", "PREPARING"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/list"))
                .andExpect(model().attributeExists("orders"));
    }

    // ---------- GET /admin/orders/new ----------

    @Test
    @DisplayName("GET /admin/orders/new retorna formulário de novo pedido")
    void newOrder_retornaFormulario() throws Exception {
        mockMvc.perform(get("/admin/orders/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/form"))
                .andExpect(model().attributeExists("order"));
    }

    // ---------- GET /admin/orders/{id} ----------

    @Test
    @DisplayName("GET /admin/orders/{id} retorna formulário com pedido existente")
    void findById_pedidoExistente_retornaFormulario() throws Exception {
        var dto = orderResponseDTO(5L, "PENDING");
        when(orderService.findById(5L)).thenReturn(dto);

        mockMvc.perform(get("/admin/orders/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/form"))
                .andExpect(model().attribute("order", dto));
    }

    // ---------- POST /admin/orders/new — criação ----------

    @Test
    @DisplayName("POST /admin/orders/new com dados válidos cria pedido e redireciona")
    void create_dadosValidos_redirecionaComSucesso() throws Exception {
        when(orderService.create(any(OrderCreateDTO.class))).thenReturn(orderResponseDTO(10L, "PENDING"));

        mockMvc.perform(post("/admin/orders/new")
                        .param("clientId", "1")
                        .param("products[0].productId", "1")
                        .param("products[0].quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"))
                .andExpect(flash().attribute("successMessage", "Pedido criado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/orders/new sem clientId falha na validação e retorna formulário")
    void create_semClientId_retornaFormularioComErros() throws Exception {
        mockMvc.perform(post("/admin/orders/new")
                        .param("products[0].productId", "1")
                        .param("products[0].quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/form"));
    }

    @Test
    @DisplayName("POST /admin/orders/new com BusinessException redireciona com mensagem de erro")
    void create_businessException_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Produto indisponível"))
                .when(orderService).create(any(OrderCreateDTO.class));

        mockMvc.perform(post("/admin/orders/new")
                        .param("clientId", "1")
                        .param("products[0].productId", "1")
                        .param("products[0].quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders/new"))
                .andExpect(flash().attribute("errorMessage", "Produto indisponível"));
    }

    // ---------- POST /admin/orders/{id}/status — avanço de status ----------

    @Test
    @DisplayName("POST /admin/orders/{id}/status com status válido redireciona com sucesso")
    void updateStatus_statusValido_redirecionaComSucesso() throws Exception {
        doNothing().when(orderService).updateStatus(eq(3L), eq(OrderStatus.PREPARING));

        mockMvc.perform(post("/admin/orders/3/status")
                        .param("status", "PREPARING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"))
                .andExpect(flash().attribute("successMessage", "Status atualizado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/status com BusinessException redireciona com mensagem de erro")
    void updateStatus_businessException_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Transição de status inválida"))
                .when(orderService).updateStatus(eq(3L), eq(OrderStatus.DELIVERED));

        mockMvc.perform(post("/admin/orders/3/status")
                        .param("status", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"))
                .andExpect(flash().attribute("errorMessage", "Transição de status inválida"));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/status com status inválido redireciona com mensagem de erro")
    void updateStatus_statusInvalido_redirecionaComErro() throws Exception {
        mockMvc.perform(post("/admin/orders/3/status")
                        .param("status", "STATUS_INEXISTENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- POST /admin/orders/{id}/delete — exclusão ----------

    @Test
    @DisplayName("POST /admin/orders/{id}/delete exclui pedido e redireciona com sucesso")
    void delete_pedidoExistente_redirecionaComSucesso() throws Exception {
        doNothing().when(orderService).delete(7L);

        mockMvc.perform(post("/admin/orders/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"))
                .andExpect(flash().attribute("successMessage", "Pedido deletado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/delete com BusinessException redireciona com mensagem de erro")
    void delete_businessException_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Pedido não pode ser excluído"))
                .when(orderService).delete(7L);

        mockMvc.perform(post("/admin/orders/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"))
                .andExpect(flash().attribute("errorMessage", "Pedido não pode ser excluído"));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/delete com NotFoundException redireciona com mensagem de erro")
    void delete_naoEncontrado_redirecionaComErro() throws Exception {
        doThrow(new NotFoundException("Pedido não encontrado com ID: 99"))
                .when(orderService).delete(99L);

        mockMvc.perform(post("/admin/orders/99/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/orders*"));
    }

    // ---------- Auxiliares ----------

    private OrderResponseDTO orderResponseDTO(Long id, String status) {
        var client = new ClientDTO();
        client.setId(1L);
        client.setName("Cliente Teste");

        var product = new ProductOrderDTO();
        product.setProductId(1L);
        product.setProductName("Produto Teste");
        product.setQuantity(2);
        product.setPriceAtTime(new BigDecimal("25.00"));

        var dto = new OrderResponseDTO();
        dto.setId(id);
        dto.setClient(client);
        dto.setProducts(List.of(product));
        dto.setStatus(status);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTotalValue(new BigDecimal("50.00"));
        return dto;
    }
}
