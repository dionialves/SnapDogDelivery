package com.dionialves.snapdogdelivery.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> searchByName(
            @RequestParam(required = false, defaultValue = "") String q) {

        List<ClientDTO> clients = clientService.searchByNameOrPhone(q);
        return ResponseEntity.ok(clients);

    }
}
