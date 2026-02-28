package com.dionialves.snapdogdelivery.domain.admin.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.Product;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.admin.productorder.dto.ProductOrderDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pedido não encontrado com ID: " + id));

        return OrderResponseDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> search(OrderStatus status, String customerSearch) {

        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.customerSearchContains(customerSearch));

        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> search(OrderStatus status, String customerSearch, int page, int size) {

        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.customerSearchContains(customerSearch));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return orderRepository.findAll(spec, pageable)
                .map(OrderResponseDTO::fromEntity);
    }

    /**
     * Lista os pedidos de um cliente específico de forma paginada.
     * Usado pela área do cliente (/account/orders).
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(OrderResponseDTO::fromEntity);
    }

    /**
     * Retorna os 5 pedidos mais recentes de um cliente (painel da conta).
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findRecentByCustomerId(Long customerId) {
        return orderRepository.findTop5ByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public OrderResponseDTO create(OrderCreateDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new NotFoundException(
                        "Cliente não encontrado com ID: " + dto.getCustomerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedAt(dto.getCreatedAt());
        order.setOrigin(dto.getOrigin() != null ? dto.getOrigin() : OrderOrigin.MANUAL);

        processProducts(order, dto.getProducts());
        Order saved = orderRepository.save(order);

        return OrderResponseDTO.fromEntity(saved);

    }

    @Transactional
    public void updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado com ID: " + id));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus.isFinal()) {
            throw new BusinessException("Pedidos com status '"
                    + currentStatus.name() + "' não podem ser alterados");
        }

        if (newStatus == OrderStatus.CANCELED) {
            if (!currentStatus.canCancel()) {
                throw new BusinessException("Pedido só pode ser cancelado no status PENDENTE");
            }
        } else if (!currentStatus.canAdvanceTo(newStatus)) {
            throw new BusinessException("Transição de status inválida: "
                    + currentStatus.name() + " → " + newStatus.name());
        }

        order.setStatus(newStatus);
    }

    public void update(Long id, OrderCreateDTO orderDTO) {
        if (!orderRepository.existsById(id))
            throw new NotFoundException("Pedido não encontrado com ID: " + id);

        throw new BusinessException("Pedidos não podem ser modificados após a criação");
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException(
                    "Pedido não encontrado com ID: " + id);
        }
        orderRepository.deleteById(id);
    }

    private void processProducts(Order order, List<ProductOrderDTO> productOrderDTOs) {

        for (ProductOrderDTO productOrderDTO : productOrderDTOs) {

            Product product = productRepository.findById(productOrderDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Produto não encontrado com ID: " + productOrderDTO.getProductId()));

            order.addProduct(product, productOrderDTO.getQuantity(), product.getPrice());
        }
    }
}
