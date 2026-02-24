package com.dionialves.snapdogdelivery.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pedido não encontrado com ID: " + id));

        return OrderResponseDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> search(OrderStatus status, String clientSearch) {

        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.clientSearchContains(clientSearch));

        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> search(OrderStatus status, String clientSearch, int page, int size) {

        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.clientSearchContains(clientSearch));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return orderRepository.findAll(spec, pageable)
                .map(OrderResponseDTO::fromEntity);
    }

    @Transactional
    public OrderResponseDTO create(OrderCreateDTO dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException(
                        "Cliente não encontrado com ID: " + dto.getClientId()));

        Order order = new Order();
        order.setClient(client);
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
