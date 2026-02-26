package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<CustomerDTO> search(String search) {

        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search)
                .stream()
                .map(CustomerDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> search(String search, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search, pageable)
                .map(CustomerDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public CustomerDTO findById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Cliente não encontrado com ID: " + id));

        return CustomerDTO.fromEntity(customer);

    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> searchByNameOrPhone(String search) {

        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(search, search)
                .stream()
                .map(CustomerDTO::fromEntity)
                .toList();
    }

    public CustomerDTO create(CustomerDTO customer) {

        Customer saved = new Customer();
        saved.setName(customer.getName());
        saved.setPhone(customer.getPhone());
        saved.setEmail(customer.getEmail());
        saved.setCity(customer.getCity());
        saved.setState(customer.getState());
        saved.setNeighborhood(customer.getNeighborhood());
        saved.setStreet(customer.getStreet());
        saved.setZipCode(customer.getZipCode());
        saved.setNumber(customer.getNumber());
        saved.setComplement(customer.getComplement());

        customerRepository.save(saved);
        return CustomerDTO.fromEntity(saved);

    }

    @Transactional
    public CustomerDTO update(Long id, CustomerDTO customer) {

        Customer updating = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Cliente não encontrado com ID: " + id));

        updating.setName(customer.getName());
        updating.setPhone(customer.getPhone());
        updating.setEmail(customer.getEmail());
        updating.setCity(customer.getCity());
        updating.setState(customer.getState());
        updating.setNeighborhood(customer.getNeighborhood());
        updating.setStreet(customer.getStreet());
        updating.setZipCode(customer.getZipCode());
        updating.setNumber(customer.getNumber());
        updating.setComplement(customer.getComplement());

        return CustomerDTO.fromEntity(updating);

    }

    @Transactional
    public void delete(Long id) {

        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado com ID: " + id);
        }

        if (orderRepository.existsByCustomerId(id)) {
            throw new BusinessException("Cliente não pode ser excluído pois possui pedidos associados");
        }
        customerRepository.deleteById(id);

    }
}
