package com.dionialves.snapdogdelivery.domain.storefront.checkout;

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
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderService;
import com.dionialves.snapdogdelivery.domain.admin.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.domain.storefront.cart.Cart;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private Customer customer;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
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

        // Configura o principal no SecurityContextHolder para que @AuthenticationPrincipal funcione
        var principal = new User("joao@email.com", "senha",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- GET /checkout ----------

    @Test
    @DisplayName("GET /checkout com carrinho com itens exibe tela de revisão")
    void reviewPage_carrinhoComItens_retornaReview() throws Exception {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, new BigDecimal("15.90"), 2);

        when(cartService.getCart(any())).thenReturn(cart);
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(cartService.getItemCount(any())).thenReturn(2);

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/checkout/review"))
                .andExpect(model().attributeExists("cart", "customer", "cartItemCount"));
    }

    @Test
    @DisplayName("GET /checkout com carrinho vazio redireciona para /catalog")
    void reviewPage_carrinhoVazio_redirecionaParaCatalogo() throws Exception {
        var cart = new Cart();
        when(cartService.getCart(any())).thenReturn(cart);

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"));
    }

    // ---------- POST /checkout/confirm ----------

    @Test
    @DisplayName("POST /checkout/confirm com sucesso redireciona para a tela de confirmação")
    void confirm_sucesso_redirecionaParaConfirmacao() throws Exception {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));

        var orderDTO = buildOrderDTO(42L);
        when(checkoutService.createOrderFromCart(any(), any())).thenReturn(orderDTO);

        mockMvc.perform(post("/checkout/confirm").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/confirmation/42"));
    }

    @Test
    @DisplayName("POST /checkout/confirm com BusinessException redireciona para /checkout com errorMessage")
    void confirm_businessException_redirecionaComErro() throws Exception {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        doThrow(new BusinessException("Carrinho vazio."))
                .when(checkoutService).createOrderFromCart(any(), any());

        mockMvc.perform(post("/checkout/confirm").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"))
                .andExpect(flash().attribute("errorMessage", "Carrinho vazio."));
    }

    // ---------- GET /checkout/confirmation/{orderId} ----------

    @Test
    @DisplayName("GET /checkout/confirmation/{id} com pedido do cliente retorna tela de confirmação")
    void confirmation_pedidoDoCliente_retornaConfirmacao() throws Exception {
        var customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("João Silva");

        var orderDTO = buildOrderDTO(10L);
        orderDTO.setCustomer(customerDTO);

        when(orderService.findById(10L)).thenReturn(orderDTO);
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/checkout/confirmation/10").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/checkout/confirmation"))
                .andExpect(model().attributeExists("order", "cartItemCount"));
    }

    @Test
    @DisplayName("GET /checkout/confirmation/{id} com pedido de outro cliente redireciona para /account")
    void confirmation_pedidoDeOutroCliente_redirecionaParaAccount() throws Exception {
        var outraCustomerDTO = new CustomerDTO();
        outraCustomerDTO.setId(99L);

        var orderDTO = buildOrderDTO(10L);
        orderDTO.setCustomer(outraCustomerDTO);

        when(orderService.findById(10L)).thenReturn(orderDTO);
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/checkout/confirmation/10").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
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
        customerDTO.setName("João Silva");
        dto.setCustomer(customerDTO);
        dto.setProducts(List.of());
        return dto;
    }
}
