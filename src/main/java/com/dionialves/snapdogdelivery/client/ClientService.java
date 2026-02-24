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
                        "Cliente não encontrado com ID: " + id));

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

        Client saved = new Client();
        saved.setName(client.getName());
        saved.setPhone(client.getPhone());
        saved.setEmail(client.getEmail());
        saved.setCity(client.getCity());
        saved.setState(client.getState());
        saved.setNeighborhood(client.getNeighborhood());
        saved.setStreet(client.getStreet());
        saved.setZipCode(client.getZipCode());
        saved.setNumber(client.getNumber());
        saved.setComplement(client.getComplement());

        clientRepository.save(saved);
        return ClientDTO.fromEntity(saved);

    }

    @Transactional
    public ClientDTO update(Long id, ClientDTO client) {

        Client updating = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Cliente não encontrado com ID: " + id));

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

    @Transactional
    public void delete(Long id) {

        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado com ID: " + id);
        }

        if (orderRepository.existsByClientId(id)) {
            throw new BusinessException("Cliente não pode ser excluído pois possui pedidos associados");
        }
        clientRepository.deleteById(id);

    }
}
