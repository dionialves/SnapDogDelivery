package com.dionialves.snapdogdelivery.domain.admin.order;

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

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.order.Order;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderStatus;
import com.dionialves.snapdogdelivery.domain.admin.product.Product;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.admin.productorder.ProductOrder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("João Silva");
        customer.setPhone("(11) 91234-5678");
        customer.setEmail("joao@email.com");
        customer.setCity("São Paulo");
        customer.setState(State.SP);
        customer.setNeighborhood("Centro");
        customer.setStreet("Rua das Flores");
        customer.setZipCode("01310-100");
        customer.setNumber("10");
        customer.setPassword("$2a$10$hashed_password_for_test");
        customer.setActive(true);
        customer = customerRepository.save(customer);

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
        orderRepository.save(createOrder(today.atTime(10, 0)));
        orderRepository.save(createOrder(today.minusDays(1).atTime(10, 0)));

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
        orderRepository.save(createOrderWithProduct(today.atTime(10, 0), 2)); // 2 × 15.90 = 31.80
        orderRepository.save(createOrderWithProduct(today.minusDays(1).atTime(10, 0), 1)); // 15.90

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

    // --- existsByCustomerId ---

    @Test
    @DisplayName("existsByCustomerId retorna true quando cliente tem pedido")
    void existsByCustomerId_comPedido_retornaTrue() {
        orderRepository.save(createOrder(LocalDateTime.now()));

        assertThat(orderRepository.existsByCustomerId(customer.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByCustomerId retorna false quando cliente não tem pedido")
    void existsByCustomerId_semPedido_retornaFalse() {
        assertThat(orderRepository.existsByCustomerId(customer.getId())).isFalse();
    }

    // --- findTopSellingProducts ---

    @Test
    @DisplayName("findTopSellingProducts retorna produtos ordenados por quantidade vendida")
    void findTopSellingProducts_retornaOrdenadoPorQuantidade() {
        orderRepository.save(createOrderWithProduct(LocalDateTime.now(), 3));
        orderRepository.save(createOrderWithProduct(LocalDateTime.now().minusHours(1), 5));

        var result = orderRepository.findTopSellingProducts(PageRequest.of(0, 5));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hot Dog Clássico");
        assertThat(result.get(0).getTotalSold()).isEqualTo(8L); // 3 + 5
    }

    // --- helpers privados ---

    private Order createOrder(LocalDateTime createdAt) {
        var order = new Order();
        order.setCustomer(customer);
        order.setCreatedAt(createdAt);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private Order createOrderWithProduct(LocalDateTime createdAt, int quantity) {
        var order = createOrder(createdAt);
        var po = new ProductOrder(product, order, quantity, product.getPrice());
        order.getProductOrders().add(po);
        return order;
    }
}
