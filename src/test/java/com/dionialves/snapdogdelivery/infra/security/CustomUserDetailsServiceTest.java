package com.dionialves.snapdogdelivery.infra.security;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.dionialves.snapdogdelivery.domain.admin.user.Role;
import com.dionialves.snapdogdelivery.domain.admin.user.User;
import com.dionialves.snapdogdelivery.domain.admin.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Admin Teste");
        user.setEmail("admin@snapdog.com");
        user.setPassword("$2a$10$encodedAdminPassword");
        user.setRole(Role.ADMIN);
    }

    @Test
    @DisplayName("loadUserByUsername com e-mail existente retorna UserDetails com dados corretos")
    void loadUserByUsername_usuarioExistente_retornaUserDetails() {
        when(userRepository.findByEmail("admin@snapdog.com")).thenReturn(Optional.of(user));

        var result = customUserDetailsService.loadUserByUsername("admin@snapdog.com");

        assertThat(result.getUsername()).isEqualTo("admin@snapdog.com");
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedAdminPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername com e-mail não cadastrado lança UsernameNotFoundException com o e-mail na mensagem")
    void loadUserByUsername_emailNaoEncontrado_lancaUsernameNotFoundException() {
        when(userRepository.findByEmail("inexistente@snapdog.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inexistente@snapdog.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inexistente@snapdog.com");
    }

    @Test
    @DisplayName("loadUserByUsername com role SUPER_ADMIN retorna authority ROLE_SUPER_ADMIN")
    void loadUserByUsername_superAdmin_retornaAuthorityCorreta() {
        user.setRole(Role.SUPER_ADMIN);
        when(userRepository.findByEmail("super@snapdog.com")).thenReturn(Optional.of(user));
        user.setEmail("super@snapdog.com");

        var result = customUserDetailsService.loadUserByUsername("super@snapdog.com");

        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_SUPER_ADMIN");
    }
}
