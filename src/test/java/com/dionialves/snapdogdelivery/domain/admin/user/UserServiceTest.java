package com.dionialves.snapdogdelivery.domain.admin.user;

import java.time.LocalDateTime;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dionialves.snapdogdelivery.domain.admin.user.Role;
import com.dionialves.snapdogdelivery.domain.admin.user.User;
import com.dionialves.snapdogdelivery.domain.admin.user.UserRepository;
import com.dionialves.snapdogdelivery.domain.admin.user.UserService;
import com.dionialves.snapdogdelivery.domain.admin.user.dto.UserCreateDTO;
import com.dionialves.snapdogdelivery.domain.admin.user.dto.UserResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.user.dto.UserUpdateDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Administrador Teste");
        user.setEmail("admin@snapdog.com");
        user.setPassword("$2a$10$encoded");
        user.setRole(Role.ADMIN);
        user.setCreatedAt(LocalDateTime.now());

        createDTO = new UserCreateDTO();
        createDTO.setName("Novo Admin");
        createDTO.setEmail("novo@snapdog.com");
        createDTO.setPassword("senha123");
        createDTO.setRole(Role.ADMIN);

        updateDTO = new UserUpdateDTO();
        updateDTO.setName("Admin Atualizado");
        updateDTO.setRole(Role.SUPER_ADMIN);
    }

    // --- findAll ---

    @Test
    @DisplayName("findAll retorna página de usuários")
    void findAll_retornaPaginaUsuarios() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserResponseDTO> result = userService.findAll(0);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Administrador Teste");
    }

    // --- findById ---

    @Test
    @DisplayName("findById com ID existente retorna DTO")
    void findById_existente_retornaDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("admin@snapdog.com");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("findById com ID inexistente lança NotFoundException")
    void findById_inexistente_lancaNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    @DisplayName("create com e-mail único persiste e retorna DTO")
    void create_emailUnico_persisteERetornaDTO() {
        when(userRepository.existsByEmail("novo@snapdog.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        var result = userService.create(createDTO);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("novo@snapdog.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("create com e-mail duplicado lança BusinessException")
    void create_emailDuplicado_lancaBusinessException() {
        when(userRepository.existsByEmail("novo@snapdog.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(createDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("novo@snapdog.com");

        verify(userRepository, never()).save(any());
    }

    // --- update ---

    @Test
    @DisplayName("update com ID existente atualiza nome e role")
    void update_existente_atualizaNomeERole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.update(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Admin Atualizado");
        assertThat(result.getRole()).isEqualTo(Role.SUPER_ADMIN);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("update com senha preenchida atualiza a senha")
    void update_comSenha_atualizaSenha() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaSenha")).thenReturn("$2a$nova");

        updateDTO.setPassword("novaSenha");
        userService.update(1L, updateDTO);

        verify(passwordEncoder).encode("novaSenha");
    }

    @Test
    @DisplayName("update com senha em branco não altera a senha")
    void update_semSenha_mantemSenhaAtual() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        updateDTO.setPassword("  ");
        userService.update(1L, updateDTO);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("update com ID inexistente lança NotFoundException")
    void update_inexistente_lancaNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    @DisplayName("delete com ID existente e usuário diferente remove com sucesso")
    void delete_existente_outroUsuario_removeComSucesso() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simula contexto de segurança com outro usuário logado
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("outro@snapdog.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("delete do próprio usuário autenticado lança BusinessException")
    void delete_proprioUsuario_lancaBusinessException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simula contexto de segurança com o mesmo e-mail do usuário
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@snapdog.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("próprio usuário");

        verify(userRepository, never()).deleteById(any());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("delete com ID inexistente lança NotFoundException")
    void delete_inexistente_lancaNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).deleteById(any());
    }
}
