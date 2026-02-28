package com.dionialves.snapdogdelivery.domain.storefront.auth;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerUserDetailsService customerUserDetailsService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("João Silva");
        customer.setEmail("joao@email.com");
        customer.setPhone("(11) 91234-5678");
        customer.setCity("São Paulo");
        customer.setState(State.SP);
        customer.setNeighborhood("Centro");
        customer.setStreet("Rua das Flores");
        customer.setZipCode("01310-100");
        customer.setNumber("10");
        customer.setPassword("$2a$10$encodedPassword");
        customer.setActive(true);
    }

    @Test
    @DisplayName("loadUserByUsername com e-mail existente e ativo retorna UserDetails com ROLE_CUSTOMER")
    void loadUserByUsername_clienteAtivo_retornaUserDetails() {
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));

        var result = customerUserDetailsService.loadUserByUsername("joao@email.com");

        assertThat(result.getUsername()).isEqualTo("joao@email.com");
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("loadUserByUsername com e-mail não cadastrado lança UsernameNotFoundException")
    void loadUserByUsername_emailNaoEncontrado_lancaUsernameNotFoundException() {
        when(customerRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername("inexistente@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inexistente@email.com");
    }

    @Test
    @DisplayName("loadUserByUsername com cliente inativo lança DisabledException")
    void loadUserByUsername_clienteInativo_lancaDisabledException() {
        customer.setActive(false);
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername("joao@email.com"))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("joao@email.com");
    }
}
