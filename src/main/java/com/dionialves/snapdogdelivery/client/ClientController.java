package com.dionialves.snapdogdelivery.client;

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

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/clients")
    public ResponseEntity<List<ClientDTO>> findAll() {

        List<ClientDTO> clients = clientService.findAll();
        return ResponseEntity.ok(clients);

    }

    @GetMapping("/client/{id}")
    public ResponseEntity<ClientDTO> findById(@PathVariable Long id) {

        ClientDTO client = clientService.findById(id);
        return ResponseEntity.ok(client);

    }

    @PostMapping("/client")
    public ResponseEntity<ClientDTO> create(@Valid @RequestBody ClientDTO client) {

        ClientDTO created = clientService.create(client);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);

    }

    @PutMapping("/client/{id}")
    public ResponseEntity<ClientDTO> update(@Valid @RequestBody ClientDTO client, @PathVariable Long id) {

        ClientDTO response = clientService.update(id, client);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("client/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        clientService.delete(id);
        return ResponseEntity.noContent().build();

    }
}
