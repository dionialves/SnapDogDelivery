package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customerJohn;
    private Customer customerMary;

    @BeforeEach
    void setUp() {
        customerJohn = customerRepository.save(createCustomer("João Silva", "(11) 91234-5678", "joao@email.com"));
        customerMary = customerRepository.save(createCustomer("Maria Santos", "(21) 99876-5432", "maria@email.com"));
    }

    // --- findByNameContainingIgnoreCaseOrPhoneContaining (lista) ---

    @Test
    @DisplayName("findByNameOrPhone por nome (case-insensitive) retorna cliente correto")
    void findByNameOrPhone_porNome_retornaCliente() {
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("joão", "joão");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("findByNameOrPhone por telefone retorna cliente correto")
    void findByNameOrPhone_porTelefone_retornaCliente() {
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("99876", "99876");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Santos");
    }

    @Test
    @DisplayName("findByNameOrPhone com termo parcial retorna resultado correto")
    void findByNameOrPhone_termoGeral_retornaResultado() {
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("silva", "silva");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findByNameOrPhone sem correspondência retorna lista vazia")
    void findByNameOrPhone_semCorrespondencia_retornaVazio() {
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("xyz", "xyz");

        assertThat(result).isEmpty();
    }

    // --- findByNameOrPhone paginado ---

    @Test
    @DisplayName("findByNameOrPhone paginado retorna Page com resultado correto")
    void findByNameOrPhone_paginado_retornaPage() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("", "", pageable);

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("findByNameOrPhone paginado com tamanho 1 retorna apenas 1 resultado por página")
    void findByNameOrPhone_paginadoTamanho1_retornaUmPorPagina() {
        var pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "name"));
        var result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContaining("", "", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    // --- findByEmail ---

    @Test
    @DisplayName("findByEmail com e-mail existente retorna o cliente correto")
    void findByEmail_existente_retornaCliente() {
        var result = customerRepository.findByEmail("joao@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findByEmail com e-mail inexistente retorna Optional vazio")
    void findByEmail_inexistente_retornaVazio() {
        var result = customerRepository.findByEmail("naoexiste@email.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail retorna true para e-mail cadastrado")
    void existsByEmail_cadastrado_retornaTrue() {
        assertThat(customerRepository.existsByEmail("maria@email.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail retorna false para e-mail não cadastrado")
    void existsByEmail_naoCadastrado_retornaFalse() {
        assertThat(customerRepository.existsByEmail("outro@email.com")).isFalse();
    }

    // --- countByCreatedAt ---

    @Test
    @DisplayName("countByCreatedAt conta clientes criados hoje")
    void countByCreatedAt_hoje_retornaContagem() {
        // customers criados no setUp usam LocalDate.now() via inicializador de campo
        long count = customerRepository.countByCreatedAt(LocalDate.now());

        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("countByCreatedAt para data sem clientes retorna zero")
    void countByCreatedAt_dataSemClientes_retornaZero() {
        long count = customerRepository.countByCreatedAt(LocalDate.now().minusYears(1));

        assertThat(count).isEqualTo(0);
    }

    // --- helper privado ---

    private Customer createCustomer(String name, String phone, String email) {
        var c = new Customer();
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        c.setCity("São Paulo");
        c.setState(State.SP);
        c.setNeighborhood("Centro");
        c.setStreet("Rua das Flores");
        c.setZipCode("01310-100");
        c.setNumber("10");
        c.setPassword("$2a$10$hashed_password_for_test");
        c.setActive(true);
        return c;
    }
}
