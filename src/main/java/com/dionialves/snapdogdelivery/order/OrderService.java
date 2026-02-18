package com.dionialves.snapdogdelivery.order;

import java.util.List;

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
                        "Order not found with ID: " + id));

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

    @Transactional
    public OrderResponseDTO create(OrderCreateDTO dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + dto.getClientId()));

        Order order = new Order();
        order.setClient(client);
        order.setCreatedAt(dto.getCreatedAt());

        processProducts(order, dto.getProducts());
        Order saved = orderRepository.save(order);

        System.out.println(saved);
        return OrderResponseDTO.fromEntity(saved);

    }

    @Transactional
    public void updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + id));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus.isFinal()) {
            throw new BusinessException("Orders with status '"
                    + currentStatus.name() + "' cannot be changed");
        }

        if (newStatus == OrderStatus.CANCELED) {
            if (!currentStatus.canCancel()) {
                throw new BusinessException("Order can only be canceled in PENDING status");
            }
        } else if (!currentStatus.canAdvanceTo(newStatus)) {
            throw new BusinessException("Invalid status transition: "
                    + currentStatus.name() + " → " + newStatus.name());
        }

        order.setStatus(newStatus);
    }

    public void update(Long id, OrderCreateDTO orderDTO) {
        if (!orderRepository.existsById(id))
            throw new NotFoundException("Order not found with ID: " + id);

        throw new BusinessException("Orders cannot be modified after creation");
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException(
                    "Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }

    private void processProducts(Order order, List<ProductOrderDTO> productOrderDTOs) {

        for (ProductOrderDTO productOrderDTO : productOrderDTOs) {

            Product product = productRepository.findById(productOrderDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Product not found with ID: " + productOrderDTO.getProductId()));

            order.addProduct(product, productOrderDTO.getQuantity(), product.getPrice());
        }
    }
}
