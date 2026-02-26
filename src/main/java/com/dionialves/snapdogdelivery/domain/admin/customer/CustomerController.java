package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> search(@RequestParam(name = "q", defaultValue = "") String q) {

        List<CustomerDTO> customers = customerService.search(q);

        return ResponseEntity.ok(customers);
    }

}
