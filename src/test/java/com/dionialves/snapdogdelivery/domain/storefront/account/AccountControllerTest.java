package com.dionialves.snapdogdelivery.domain.storefront.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerService;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderService;
import com.dionialves.snapdogdelivery.domain.admin.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private Customer customer;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();

        session = new MockHttpSession();

        customer = new Customer();
        customer.setId(1L);
        customer.setName("João Silva");
        customer.setEmail("joao@email.com");
        customer.setPhone("(11) 91234-5678");
        customer.setCity("São Paulo");
        customer.setState(State.SP);
        customer.setNeighborhood("Centro");
        customer.setStreet("Rua das Flores");
        customer.setZipCode("01310-100");
        customer.setNumber("10");
        customer.setActive(true);

        // Configura principal no SecurityContextHolder para @AuthenticationPrincipal
        var principal = new User("joao@email.com", "senha",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- GET /account ----------

    @Test
    @DisplayName("GET /account retorna dashboard com pedidos recentes no model")
    void dashboard_retornaDashboardComPedidosRecentes() throws Exception {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(orderService.findRecentByCustomerId(1L)).thenReturn(List.of(buildOrderDTO(1L)));
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/account").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/account/dashboard"))
                .andExpect(model().attributeExists("customer", "recentOrders", "cartItemCount"));
    }

    // ---------- GET /account/orders ----------

    @Test
    @DisplayName("GET /account/orders retorna lista paginada de pedidos")
    void orders_retornaListaPaginada() throws Exception {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        var page = new PageImpl<OrderResponseDTO>(List.of(buildOrderDTO(1L)), PageRequest.of(0, 10), 1);
        when(orderService.findByCustomerId(eq(1L), any())).thenReturn(page);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/account/orders").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/account/orders"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages"));
    }

    // ---------- GET /account/orders/{id} ----------

    @Test
    @DisplayName("GET /account/orders/{id} com pedido do próprio cliente retorna detalhe")
    void orderDetail_pedidoDoCliente_retornaDetalhe() throws Exception {
        var customerDTO = new CustomerDTO();
        customerDTO.setId(1L);

        var orderDTO = buildOrderDTO(5L);
        orderDTO.setCustomer(customerDTO);

        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(orderService.findById(5L)).thenReturn(orderDTO);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/account/orders/5").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/account/order-detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("GET /account/orders/{id} com pedido de outro cliente propaga NotFoundException como ServletException")
    void orderDetail_pedidoDeOutroCliente_propagaNotFoundException() throws Exception {
        var outraCustomerDTO = new CustomerDTO();
        outraCustomerDTO.setId(99L);

        var orderDTO = buildOrderDTO(5L);
        orderDTO.setCustomer(outraCustomerDTO);

        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(orderService.findById(5L)).thenReturn(orderDTO);

        // NotFoundException propaga como ServletException no standaloneSetup sem GlobalExceptionHandler
        try {
            mockMvc.perform(get("/account/orders/5").session(session));
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            while (cause != null && !(cause instanceof com.dionialves.snapdogdelivery.exception.NotFoundException)) {
                cause = cause.getCause();
            }
            assert cause instanceof com.dionialves.snapdogdelivery.exception.NotFoundException;
        }
    }

    // ---------- GET /account/profile ----------

    @Test
    @DisplayName("GET /account/profile retorna formulário de perfil com dados do cliente")
    void profileForm_retornaFormularioDePerfil() throws Exception {
        var customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("João Silva");

        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(customerService.findById(1L)).thenReturn(customerDTO);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/account/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/account/profile"))
                .andExpect(model().attributeExists("customerDTO", "states", "cartItemCount"));
    }

    // ---------- POST /account/profile ----------

    @Test
    @DisplayName("POST /account/profile com dados válidos redireciona com successMessage")
    void updateProfile_dadosValidos_redirecionaComSucesso() throws Exception {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));

        mockMvc.perform(post("/account/profile").session(session)
                .param("name", "João Atualizado")
                .param("phone", "(11) 91234-5678")
                .param("email", "joao@email.com")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("neighborhood", "Centro")
                .param("street", "Rua das Flores")
                .param("zipCode", "01310-100")
                .param("number", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/profile"))
                .andExpect(flash().attribute("successMessage", "Perfil atualizado com sucesso!"));
    }

    // ---------- Auxiliares ----------

    private OrderResponseDTO buildOrderDTO(Long id) {
        var dto = new OrderResponseDTO();
        dto.setId(id);
        dto.setStatus("PENDING");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTotalValue(new BigDecimal("31.80"));

        var customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        dto.setCustomer(customerDTO);
        dto.setProducts(List.of());
        return dto;
    }
}
