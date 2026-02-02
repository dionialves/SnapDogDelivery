package com.dionialves.snapdogdelivery.order;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dionialves.snapdogdelivery.order.dto.OrderCreateDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.order.dto.OrderUpdateDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> findAll() {

        List<OrderResponseDTO> orders = orderService.findAll();
        return ResponseEntity.ok(orders);

    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {

        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);

    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> newOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {

        OrderResponseDTO created = orderService.create(orderCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@Valid @RequestBody OrderUpdateDTO orderUpdateDTO,
            @PathVariable Long id) {

        orderService.update(id, orderUpdateDTO);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        orderService.deleteById(id);
        return ResponseEntity.noContent().build();

    }
}
