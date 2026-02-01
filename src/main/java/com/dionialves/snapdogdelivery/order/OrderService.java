package com.dionialves.snapdogdelivery.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientDTO;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.ProductOrder;
import com.dionialves.snapdogdelivery.productorder.ProductOrderDTO;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + id));

        return convertToDTO(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public OrderResponseDTO createOrder(OrderCreateDTO dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client not found with ID: " + dto.getClientId()));

        Order order = new Order();
        order.setClient(client);
        order.setDate(dto.getDate());

        processProducts(order, dto.getProducts());
        Order saved = orderRepository.save(order);

        return convertToDTO(saved);

    }

    public void updateOrder(Long id, OrderUpdateDTO orderDTO) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + id));
        order.setDate(orderDTO.getDate());

        order.getProductOrders().clear();
        processProducts(order, orderDTO.getProducts());
        orderRepository.save(order);

    }

    private void processProducts(Order order, List<ProductOrderDTO> productOrderDTOs) {

        for (ProductOrderDTO productOrderDTO : productOrderDTOs) {

            Product product = productRepository.findById(productOrderDTO.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + productOrderDTO.getProductId()));

            order.addProduct(product, productOrderDTO.getQuantity(), product.getPrice());
        }
    }

    private OrderResponseDTO convertToDTO(Order order) {

        OrderResponseDTO orderDTO = new OrderResponseDTO();
        orderDTO.setId(order.getId());
        orderDTO.setDate(order.getDate());
        orderDTO.setTotalValue(order.getTotalValue());

        // Client
        ClientDTO client = new ClientDTO();
        client.setName(order.getClient().getName());
        client.setAddress(order.getClient().getAddress());
        orderDTO.setClient(client);

        // Product
        List<ProductOrderDTO> productOrderList = new ArrayList<>();
        for (ProductOrder productOrder : order.getProductOrders()) {
            ProductOrderDTO productDTO = new ProductOrderDTO();
            productDTO.setProductId(productOrder.getId());
            productDTO.setQuantity(productOrder.getQuantity());

            productOrderList.add(productDTO);
        }
        orderDTO.setProducts(productOrderList);

        return orderDTO;
    }
}
