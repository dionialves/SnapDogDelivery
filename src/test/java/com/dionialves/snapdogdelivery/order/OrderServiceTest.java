package com.dionialves.snapdogdelivery.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.client.State;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Client client;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setName("João Silva");
        client.setPhone("(11) 91234-5678");
        client.setEmail("joao@email.com");
        client.setCity("São Paulo");
        client.setState(State.SP);
        client.setNeighborhood("Centro");
        client.setStreet("Rua das Flores");
        client.setZipCode("01310-100");
        client.setNumber("10");

        product = new Product();
        product.setId(1L);
        product.setName("Hot Dog Clássico");
        product.setPrice(new BigDecimal("15.90"));

        order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
    }

    // --- findById ---

    @Test
    @DisplayName("findById com ID existente retorna DTO")
    void findById_existente_retornaDTO() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("findById com ID inexistente lança NotFoundException")
    void findById_inexistente_lancaNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    @DisplayName("create com dados válidos persiste e retorna DTO")
    void create_dadosValidos_persisteERetornaDTO() {
        var productOrderDTO = new ProductOrderDTO(1L, null, 2, null);
        var dto = new OrderCreateDTO(1L, List.of(productOrderDTO), LocalDateTime.now(), null);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        var result = orderService.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("create com cliente inexistente lança NotFoundException")
    void create_clienteInexistente_lancaNotFoundException() {
        var dto = new OrderCreateDTO(99L, List.of(new ProductOrderDTO(1L, null, 1, null)), LocalDateTime.now(), null);
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("create com produto inexistente lança NotFoundException")
    void create_produtoInexistente_lancaNotFoundException() {
        var dto = new OrderCreateDTO(1L, List.of(new ProductOrderDTO(99L, null, 1, null)), LocalDateTime.now(), null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- updateStatus ---

    @Test
    @DisplayName("updateStatus de PENDING para PREPARING avança corretamente")
    void updateStatus_pendingParaPreparing_sucesso() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, OrderStatus.PREPARING);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("updateStatus de PREPARING para OUT_FOR_DELIVERY avança corretamente")
    void updateStatus_preparingParaOutForDelivery_sucesso() {
        order.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, OrderStatus.OUT_FOR_DELIVERY);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("updateStatus de OUT_FOR_DELIVERY para DELIVERED avança corretamente")
    void updateStatus_outForDeliveryParaDelivered_sucesso() {
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, OrderStatus.DELIVERED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("cancelar pedido PENDING é permitido")
    void updateStatus_cancelarPending_sucesso() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, OrderStatus.CANCELED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("cancelar pedido PREPARING lança BusinessException")
    void updateStatus_cancelarPreparing_lancaBusinessException() {
        order.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDENTE");
    }

    @Test
    @DisplayName("transição inválida (PENDING → DELIVERED) lança BusinessException")
    void updateStatus_transicaoInvalida_lancaBusinessException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.DELIVERED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("alterar pedido DELIVERED (final) lança BusinessException")
    void updateStatus_pedidoFinal_lancaBusinessException() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("alterar pedido CANCELED (final) lança BusinessException")
    void updateStatus_pedidoCancelado_lancaBusinessException() {
        order.setStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CANCELED");
    }

    @Test
    @DisplayName("updateStatus com ID inexistente lança NotFoundException")
    void updateStatus_inexistente_lancaNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.PREPARING))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    @DisplayName("delete com ID existente remove o pedido")
    void delete_existente_removeSucesso() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.delete(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete com ID inexistente lança NotFoundException")
    void delete_inexistente_lancaNotFoundException() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).deleteById(any());
    }
}
