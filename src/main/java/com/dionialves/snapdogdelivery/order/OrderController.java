package com.dionialves.snapdogdelivery.order;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> findAll() {

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);

    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {

        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);

    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> newOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {

        OrderResponseDTO created = orderService.createOrder(orderCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);

    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> update(@Valid @RequestBody OrderUpdateDTO orderUpdateDTO,
            @PathVariable Long id) {

        orderService.updateOrder(id, orderUpdateDTO);
        return ResponseEntity.noContent().build();

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
