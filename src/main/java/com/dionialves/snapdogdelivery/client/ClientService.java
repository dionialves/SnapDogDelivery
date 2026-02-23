package com.dionialves.snapdogdelivery.client;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<ClientDTO> search(String search) {

        return clientRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search)
                .stream()
                .map(ClientDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> search(String search, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        return clientRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search, pageable)
                .map(ClientDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + id));

        return ClientDTO.fromEntity(client);

    }

    @Transactional(readOnly = true)
    public List<ClientDTO> searchByNameOrPhone(String search) {

        return clientRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search)
                .stream()
                .map(ClientDTO::fromEntity)
                .toList();
    }

    public ClientDTO create(ClientDTO client) {

        Client salved = new Client();
        salved.setName(client.getName());
        salved.setPhone(client.getPhone());
        salved.setEmail(client.getEmail());
        salved.setCity(client.getCity());
        salved.setState(client.getState());
        salved.setNeighborhood(client.getNeighborhood());
        salved.setStreet(client.getStreet());
        salved.setZipCode(client.getZipCode());
        salved.setNumber(client.getNumber());
        salved.setComplement(client.getComplement());

        clientRepository.save(salved);
        return ClientDTO.fromEntity(salved);

    }

    @Transactional
    public ClientDTO update(Long id, ClientDTO client) {

        Client updating = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Client not found with ID: " + id));

        updating.setName(client.getName());
        updating.setPhone(client.getPhone());
        updating.setEmail(client.getEmail());
        updating.setCity(client.getCity());
        updating.setState(client.getState());
        updating.setNeighborhood(client.getNeighborhood());
        updating.setStreet(client.getStreet());
        updating.setZipCode(client.getZipCode());
        updating.setNumber(client.getNumber());
        updating.setComplement(client.getComplement());

        return ClientDTO.fromEntity(updating);

    }

    public void delete(Long id) {

        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Client not found with ID: " + id);
        }

        if (orderRepository.existsByClientId(id)) {
            throw new BusinessException("Customer cannot be deleted because it is associated with orders");
        }
        clientRepository.deleteById(id);

    }
}
