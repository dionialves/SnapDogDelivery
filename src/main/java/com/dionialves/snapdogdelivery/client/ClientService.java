package com.dionialves.snapdogdelivery.client;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dionialves.snapdogdelivery.client.dto.ClientCreateDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientResponseDTO;
import com.dionialves.snapdogdelivery.client.dto.ClientUpdateDTO;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientResponseDTO> findAll() {

        return clientRepository.findAll()
                .stream()
                .map(ClientResponseDTO::fromEntity)
                .toList();
    }

    public ClientResponseDTO findById(Long id) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + id));

        return ClientResponseDTO.fromEntity(client);

    }

    public ClientResponseDTO create(ClientCreateDTO client) {

        Client salved = new Client();
        salved.setName(client.getName());
        salved.setAddress(client.getAddress());

        clientRepository.save(salved);
        return ClientResponseDTO.fromEntity(salved);

    }

    public ClientResponseDTO update(Long id, ClientUpdateDTO client) {

        Client updating = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + id));

        updating.setName(client.getName());
        updating.setAddress(client.getAddress());

        clientRepository.save(updating);

        return ClientResponseDTO.fromEntity(updating);

    }

    public void delete(Long id) {

        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Client not found with ID: " + id);
        }
        clientRepository.deleteById(id);

    }
}
