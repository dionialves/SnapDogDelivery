package com.dionialves.snapdogdelivery.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.dto.ClientCreateDTO;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderUpdateDTO;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.ProductOrder;
import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found with ID: " + id));

        return OrderResponseDTO.fromEntity(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    public OrderResponseDTO createOrder(OrderCreateDTO dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + dto.getClientId()));

        Order order = new Order();
        order.setClient(client);
        order.setDate(dto.getDate());

        processProducts(order, dto.getProducts());
        Order saved = orderRepository.save(order);

        return OrderResponseDTO.fromEntity(saved);

    }

    public void updateOrder(Long id, OrderUpdateDTO orderDTO) {
        throw new BusinessException("Orders cannot be modified after creation");
    }

    public void deleteById(Long id) {
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
