package com.dionialves.snapdogdelivery.domain.admin.user;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dionialves.snapdogdelivery.domain.admin.user.Role;
import com.dionialves.snapdogdelivery.domain.admin.user.UserController;
import com.dionialves.snapdogdelivery.domain.admin.user.UserService;
import com.dionialves.snapdogdelivery.domain.admin.user.dto.UserResponseDTO;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private UserResponseDTO adminDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();

        adminDTO = new UserResponseDTO(1L, "Admin Teste", "admin@snapdog.com",
                Role.ADMIN, LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /admin/api/users retorna página de usuários")
    void findAll_retornaPaginaUsuarios() throws Exception {
        var page = new PageImpl<>(List.of(adminDTO), PageRequest.of(0, 10), 1);
        when(userService.findAll(eq(0))).thenReturn(page);

        mockMvc.perform(get("/admin/api/users").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Admin Teste"))
                .andExpect(jsonPath("$.content[0].email").value("admin@snapdog.com"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /admin/api/users/{id} retorna usuário pelo ID")
    void findById_existente_retornaUsuario() throws Exception {
        when(userService.findById(1L)).thenReturn(adminDTO);

        mockMvc.perform(get("/admin/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@snapdog.com"));
    }

    @Test
    @DisplayName("GET /admin/api/users sem parâmetro usa página 0 por padrão")
    void findAll_semParametro_usaPaginaZero() throws Exception {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(userService.findAll(0)).thenReturn(page);

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
