package com.dionialves.snapdogdelivery.client;

import java.time.LocalDate;
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

import com.dionialves.snapdogdelivery.client.dto.ClientDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.order.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setName("João Silva");
        client.setPhone("(11) 91234-5678");
        client.setEmail("joao@email.com");
        client.setCity("São Paulo");
        client.setState(State.SP);
        client.setNeighborhood("Centro");
        client.setStreet("Rua das Flores");
        client.setZipCode("01310-100");
        client.setNumber("10");
        client.setComplement("Apto 2");

        clientDTO = new ClientDTO();
        clientDTO.setName("João Silva");
        clientDTO.setPhone("(11) 91234-5678");
        clientDTO.setEmail("joao@email.com");
        clientDTO.setCity("São Paulo");
        clientDTO.setState(State.SP);
        clientDTO.setNeighborhood("Centro");
        clientDTO.setStreet("Rua das Flores");
        clientDTO.setZipCode("01310-100");
        clientDTO.setNumber("10");
    }

    // --- search (lista) ---

    @Test
    @DisplayName("search sem paginação retorna lista de DTOs")
    void search_semPaginacao_retornaListaDTOs() {
        when(clientRepository.findByNameContainingIgnoreCaseOrPhoneContaining("joão", "joão"))
                .thenReturn(List.of(client));

        var result = clientService.search("joão");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("search paginado retorna Page de DTOs")
    void search_paginado_retornaPage() {
        Page<Client> page = new PageImpl<>(List.of(client));
        when(clientRepository.findByNameContainingIgnoreCaseOrPhoneContaining(
                anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        var result = clientService.search("joão", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("joao@email.com");
    }

    // --- findById ---

    @Test
    @DisplayName("findById com ID existente retorna DTO")
    void findById_existente_retornaDTO() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        var result = clientService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findById com ID inexistente lança NotFoundException")
    void findById_inexistente_lancaNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    @DisplayName("create persiste e retorna DTO do cliente criado")
    void create_dadosValidos_persisteERetornaDTO() {
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var result = clientService.create(clientDTO);

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        verify(clientRepository).save(any(Client.class));
    }

    // --- update ---

    @Test
    @DisplayName("update com ID existente atualiza e retorna DTO")
    void update_existente_atualizaERetornaDTO() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientDTO.setName("João Atualizado");
        var result = clientService.update(1L, clientDTO);

        assertThat(result.getName()).isEqualTo("João Atualizado");
    }

    @Test
    @DisplayName("update com ID inexistente lança NotFoundException")
    void update_inexistente_lancaNotFoundException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(99L, clientDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    @DisplayName("delete com ID existente e sem pedidos remove o cliente")
    void delete_semPedidos_removeSucesso() {
        when(clientRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsByClientId(1L)).thenReturn(false);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete com ID inexistente lança NotFoundException")
    void delete_inexistente_lancaNotFoundException() {
        when(clientRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete cliente com pedidos lança BusinessException")
    void delete_comPedidos_lancaBusinessException() {
        when(clientRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsByClientId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clientService.delete(1L))
                .isInstanceOf(BusinessException.class);

        verify(clientRepository, never()).deleteById(any());
    }
}
