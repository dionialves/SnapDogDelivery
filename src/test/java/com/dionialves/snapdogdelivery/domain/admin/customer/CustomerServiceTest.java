package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("João Silva");
        customer.setPhone("(11) 91234-5678");
        customer.setEmail("joao@email.com");
        customer.setCity("São Paulo");
        customer.setState(State.SP);
        customer.setNeighborhood("Centro");
        customer.setStreet("Rua das Flores");
        customer.setZipCode("01310-100");
        customer.setNumber("10");
        customer.setComplement("Apto 2");

        customerDTO = new CustomerDTO();
        customerDTO.setName("João Silva");
        customerDTO.setPhone("(11) 91234-5678");
        customerDTO.setEmail("joao@email.com");
        customerDTO.setCity("São Paulo");
        customerDTO.setState(State.SP);
        customerDTO.setNeighborhood("Centro");
        customerDTO.setStreet("Rua das Flores");
        customerDTO.setZipCode("01310-100");
        customerDTO.setNumber("10");
    }

    // --- search (lista) ---

    @Test
    @DisplayName("search sem paginação retorna lista de DTOs")
    void search_semPaginacao_retornaListaDTOs() {
        when(customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining("joão", "joão"))
                .thenReturn(List.of(customer));

        var result = customerService.search("joão");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("search paginado retorna Page de DTOs")
    void search_paginado_retornaPage() {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(
                anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        var result = customerService.search("joão", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("joao@email.com");
    }

    // --- findById ---

    @Test
    @DisplayName("findById com ID existente retorna DTO")
    void findById_existente_retornaDTO() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        var result = customerService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findById com ID inexistente lança NotFoundException")
    void findById_inexistente_lancaNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    @DisplayName("create persiste e retorna DTO do cliente criado")
    void create_dadosValidos_persisteERetornaDTO() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var result = customerService.create(customerDTO);

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        verify(customerRepository).save(any(Customer.class));
    }

    // --- update ---

    @Test
    @DisplayName("update com ID existente atualiza e retorna DTO")
    void update_existente_atualizaERetornaDTO() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerDTO.setName("João Atualizado");
        var result = customerService.update(1L, customerDTO);

        assertThat(result.getName()).isEqualTo("João Atualizado");
    }

    @Test
    @DisplayName("update com ID inexistente lança NotFoundException")
    void update_inexistente_lancaNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(99L, customerDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    @DisplayName("delete com ID existente e sem pedidos remove o cliente")
    void delete_semPedidos_removeSucesso() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsByCustomerId(1L)).thenReturn(false);

        customerService.delete(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete com ID inexistente lança NotFoundException")
    void delete_inexistente_lancaNotFoundException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete cliente com pedidos lança BusinessException")
    void delete_comPedidos_lancaBusinessException() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(BusinessException.class);

        verify(customerRepository, never()).deleteById(any());
    }
}
