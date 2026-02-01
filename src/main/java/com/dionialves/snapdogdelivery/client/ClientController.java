package com.dionialves.snapdogdelivery.client;

import java.net.URI;
import java.util.List;

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

import com.dionialves.snapdogdelivery.client.dto.ClientCreateDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientResponseDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientUpdateDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> findAll() {

        List<ClientResponseDTO> clients = clientService.findAll();
        return ResponseEntity.ok(clients);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> findById(@PathVariable Long id) {

        ClientResponseDTO client = clientService.findById(id);
        return ResponseEntity.ok(client);

    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientCreateDTO client) {

        ClientResponseDTO created = clientService.create(client);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);

    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientUpdateDTO> update(@Valid @RequestBody ClientUpdateDTO client, @PathVariable Long id) {

        clientService.update(id, client);
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        clientService.delete(id);

    }
}
