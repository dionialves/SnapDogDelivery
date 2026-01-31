package com.dionialves.snapdogdelivery.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.ClientDTO;
import com.dionialves.snapdogdelivery.client.ClientRepository;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.product.ProductRepository;
import com.dionialves.snapdogdelivery.productorder.ProductOrder;
import com.dionialves.snapdogdelivery.productorder.ProductOrderDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<Order>> findAll() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> newOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {

        // Definicao do client
        Client client = clientRepository.findById(orderCreateDTO.getClient())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // definicao da Order
        Order newOrder = new Order();

        newOrder.setDate(orderCreateDTO.getDate());
        newOrder.setClient(client);
        newOrder.setTotalValue(20.55);

        Double totalValue = 0.0;

        // Mapeamento de productOrder

        for (ProductOrderDTO productOrderDTO : orderCreateDTO.getProducts()) {
            Product product = productRepository.findById(productOrderDTO.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + productOrderDTO.getProductId()));

            newOrder.addProduct(product, productOrderDTO.getQuantity(), product.getPrice());

            totalValue += product.getPrice() * productOrderDTO.getQuantity();
        }

        orderRepository.save(newOrder);

        // COnversacao para Order -> >>>OrderPesponseDTO

        OrderResponseDTO orderResponse = new OrderResponseDTO();
        ClientDTO clientDTO = new ClientDTO(client.getName(), client.getAddress());
        List<ProductOrderDTO> listproductOrderDTO = new ArrayList<>();

        for (ProductOrder productOrder : newOrder.getProductOrders()) {
            ProductOrderDTO productOrderDTO = new ProductOrderDTO();

            productOrderDTO.setProductId(productOrder.getId());
            productOrderDTO.setQuantity(productOrder.getQuantity());

            listproductOrderDTO.add(productOrderDTO);
        }

        orderResponse.setId(newOrder.getId());
        orderResponse.setClient(clientDTO);
        orderResponse.setProducts(listproductOrderDTO);
        orderResponse.setTotalValue(totalValue);
        orderResponse.setDate(newOrder.getDate());
        orderResponse.setTotalValue(totalValue);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);

    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderCreateDTO> update(@Valid @RequestBody OrderCreateDTO orderCreateDTO,
            @PathVariable Long id) {

        Client client = clientRepository.findById(orderCreateDTO.getClient())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Order newOrder = new Order();

        newOrder.setDate(orderCreateDTO.getDate());
        newOrder.setClient(client);
        newOrder.setTotalValue(20.55);

        // Criar uma DTO para receber a order
        // Verificar se o cliente existe
        // Verificar se a order recebeu produtos e quantidade, pois uma order não pode
        // ser criada sem Produtos
        // Verificar se o produto existe e se a quantidade é valida
        // Criar uma order
        // Criar os ProductOrder

        // Order updateOrder = orderRepository.findById(id)
        // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // updateOrder.setClient(orderCreateDTO.getClient());
        // updateOrder.setProductOrders(orderCreateDTO.getProductOrders());
        // updateOrder.setDate(orderCreateDTO.getDate());
        // updateOrder.setTotalValue(orderCreateDTO.getTotalValue());

        // Order savedOrder = orderRepository.save(updateOrder);
        // return ResponseEntity.ok(savedOrder);
        return ResponseEntity.ok(orderCreateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        orderRepository.deleteById(id);
    }
}
