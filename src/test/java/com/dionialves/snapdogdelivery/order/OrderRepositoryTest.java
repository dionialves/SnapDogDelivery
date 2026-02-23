package com.dionialves.snapdogdelivery.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.client.State;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.ProductOrder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    private Client client;
    private Product product;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setName("João Silva");
        client.setPhone("(11) 91234-5678");
        client.setEmail("joao@email.com");
        client.setCity("São Paulo");
        client.setState(State.SP);
        client.setNeighborhood("Centro");
        client.setStreet("Rua das Flores");
        client.setZipCode("01310-100");
        client.setNumber("10");
        client = clientRepository.save(client);

        product = new Product();
        product.setName("Hot Dog Clássico");
        product.setPrice(new BigDecimal("15.90"));
        product = productRepository.save(product);
    }

    // --- countByCreatedAtBetween ---

    @Test
    @DisplayName("countByCreatedAtBetween conta apenas pedidos no intervalo")
    void countByCreatedAtBetween_contaApenasNoPeriodo() {
        var today = LocalDate.now();
        orderRepository.save(criarPedido(today.atTime(10, 0)));
        orderRepository.save(criarPedido(today.minusDays(1).atTime(10, 0)));

        long count = orderRepository.countByCreatedAtBetween(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX));

        assertThat(count).isEqualTo(1);
    }

    // --- sumRevenueByCreatedAtBetween ---

    @Test
    @DisplayName("sumRevenueByCreatedAtBetween soma apenas receita do período")
    void sumRevenueByCreatedAtBetween_somaApenasDoPeriodo() {
        var today = LocalDate.now();
        orderRepository.save(criarPedidoComProduto(today.atTime(10, 0), 2));         // 2 × 15.90 = 31.80
        orderRepository.save(criarPedidoComProduto(today.minusDays(1).atTime(10, 0), 1)); // 15.90

        BigDecimal revenue = orderRepository.sumRevenueByCreatedAtBetween(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX));

        assertThat(revenue).isEqualByComparingTo("31.80");
    }

    @Test
    @DisplayName("sumRevenueByCreatedAtBetween sem pedidos retorna zero")
    void sumRevenueByCreatedAtBetween_semPedidos_retornaZero() {
        var today = LocalDate.now();

        BigDecimal revenue = orderRepository.sumRevenueByCreatedAtBetween(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX));

        assertThat(revenue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- existsByClientId ---

    @Test
    @DisplayName("existsByClientId retorna true quando cliente tem pedido")
    void existsByClientId_comPedido_retornaTrue() {
        orderRepository.save(criarPedido(LocalDateTime.now()));

        assertThat(orderRepository.existsByClientId(client.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByClientId retorna false quando cliente não tem pedido")
    void existsByClientId_semPedido_retornaFalse() {
        assertThat(orderRepository.existsByClientId(client.getId())).isFalse();
    }

    // --- findTopSellingProducts ---

    @Test
    @DisplayName("findTopSellingProducts retorna produtos ordenados por quantidade vendida")
    void findTopSellingProducts_retornaOrdenadoPorQuantidade() {
        orderRepository.save(criarPedidoComProduto(LocalDateTime.now(), 3));
        orderRepository.save(criarPedidoComProduto(LocalDateTime.now().minusHours(1), 5));

        var result = orderRepository.findTopSellingProducts(PageRequest.of(0, 5));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hot Dog Clássico");
        assertThat(result.get(0).getTotalSold()).isEqualTo(8L); // 3 + 5
    }

    // --- helpers privados ---

    private Order criarPedido(LocalDateTime createdAt) {
        var order = new Order();
        order.setClient(client);
        order.setCreatedAt(createdAt);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private Order criarPedidoComProduto(LocalDateTime createdAt, int quantidade) {
        var order = criarPedido(createdAt);
        var po = new ProductOrder(product, order, quantidade, product.getPrice());
        order.getProductOrders().add(po);
        return order;
    }
}
