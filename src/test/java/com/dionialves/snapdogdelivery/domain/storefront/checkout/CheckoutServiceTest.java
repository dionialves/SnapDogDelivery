package com.dionialves.snapdogdelivery.domain.storefront.checkout;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.order.Order;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderOrigin;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.domain.admin.product.Product;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.storefront.cart.Cart;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private HttpSession session;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        session = mock(HttpSession.class);

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

        product = new Product();
        product.setId(10L);
        product.setName("Hot Dog Clássico");
        product.setPrice(new BigDecimal("15.90"));
        product.setActive(true);
    }

    @Test
    @DisplayName("createOrderFromCart com carrinho válido cria pedido com origin ONLINE e limpa o carrinho")
    void createOrderFromCart_carrinhoValido_criaOrdemELimpaCarrinho() {
        var cart = new Cart();
        cart.addItem(10L, "Hot Dog Clássico", null, new BigDecimal("15.90"), 2);

        when(cartService.getCart(session)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        var savedOrder = new Order();
        savedOrder.setId(99L);
        savedOrder.setCustomer(customer);
        savedOrder.setOrigin(OrderOrigin.ONLINE);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        var result = checkoutService.createOrderFromCart(session, customer);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getOrigin()).isEqualTo(OrderOrigin.ONLINE);

        verify(orderRepository).save(any(Order.class));
        verify(cartService).clear(session);
    }

    @Test
    @DisplayName("createOrderFromCart com carrinho vazio lança BusinessException")
    void createOrderFromCart_carrinhoVazio_lancaBusinessException() {
        var cart = new Cart();
        when(cartService.getCart(session)).thenReturn(cart);

        assertThatThrownBy(() -> checkoutService.createOrderFromCart(session, customer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");

        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clear(any());
    }

    @Test
    @DisplayName("createOrderFromCart com produto não encontrado no repositório lança BusinessException")
    void createOrderFromCart_produtoNaoEncontrado_lancaBusinessException() {
        var cart = new Cart();
        cart.addItem(999L, "Produto Inexistente", null, new BigDecimal("10.00"), 1);

        when(cartService.getCart(session)).thenReturn(cart);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.createOrderFromCart(session, customer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Produto não encontrado");

        verify(orderRepository, never()).save(any());
    }
}
