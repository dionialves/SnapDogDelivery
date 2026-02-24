package com.dionialves.snapdogdelivery.user;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.user.dto.UserResponseDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserViewControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserViewController userViewController;

    private MockMvc mockMvc;
    private UserResponseDTO adminDTO;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(userViewController)
                .setViewResolvers(viewResolver)
                .build();

        adminDTO = new UserResponseDTO(1L, "Admin Teste", "admin@snapdog.com",
                Role.ADMIN, LocalDateTime.now());
    }

    // --- GET /admin/users ---

    @Test
    @DisplayName("GET /admin/users retorna lista de usuários no modelo")
    void list_retornaListaUsuarios() throws Exception {
        var page = new PageImpl<>(List.of(adminDTO), PageRequest.of(0, 10), 1);
        when(userService.findAll(eq(0))).thenReturn(page);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users", "currentPage", "totalPages"));
    }

    // --- GET /admin/users/new ---

    @Test
    @DisplayName("GET /admin/users/new exibe formulário de criação")
    void newForm_exibeFormulario() throws Exception {
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"))
                .andExpect(model().attributeExists("user", "roles"));
    }

    // --- POST /admin/users/new ---

    @Test
    @DisplayName("POST /admin/users/new com dados válidos redireciona com sucesso")
    void create_dadosValidos_redirecionaComSucesso() throws Exception {
        when(userService.create(any())).thenReturn(adminDTO);

        mockMvc.perform(post("/admin/users/new")
                .param("name", "Novo Admin")
                .param("email", "novo@snapdog.com")
                .param("password", "senha123")
                .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/new com e-mail duplicado redireciona com erro")
    void create_emailDuplicado_redirecionaComErro() throws Exception {
        when(userService.create(any()))
                .thenThrow(new BusinessException("Já existe um usuário com o e-mail: novo@snapdog.com"));

        mockMvc.perform(post("/admin/users/new")
                .param("name", "Novo Admin")
                .param("email", "novo@snapdog.com")
                .param("password", "senha123")
                .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/new com dados inválidos retorna formulário com erros")
    void create_dadosInvalidos_retornaFormularioComErros() throws Exception {
        mockMvc.perform(post("/admin/users/new")
                .param("name", "")
                .param("email", "emailinvalido")
                .param("password", "")
                .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"));
    }

    // --- GET /admin/users/{id} ---

    @Test
    @DisplayName("GET /admin/users/{id} exibe formulário de edição")
    void editForm_existente_exibeFormulario() throws Exception {
        when(userService.findById(1L)).thenReturn(adminDTO);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"))
                .andExpect(model().attributeExists("user", "userId", "userEmail", "roles"));
    }

    @Test
    @DisplayName("GET /admin/users/{id} com ID inexistente redireciona para listagem")
    void editForm_inexistente_redireciona() throws Exception {
        when(userService.findById(99L))
                .thenThrow(new NotFoundException("Usuário não encontrado com ID: 99"));

        mockMvc.perform(get("/admin/users/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    // --- POST /admin/users/{id} ---

    @Test
    @DisplayName("POST /admin/users/{id} com dados válidos redireciona com sucesso")
    void update_dadosValidos_redirecionaComSucesso() throws Exception {
        when(userService.update(eq(1L), any())).thenReturn(adminDTO);

        mockMvc.perform(post("/admin/users/1")
                .param("name", "Admin Atualizado")
                .param("role", "SUPER_ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // --- POST /admin/users/{id}/delete ---

    @Test
    @DisplayName("POST /admin/users/{id}/delete com sucesso redireciona com mensagem")
    void delete_sucesso_redirecionaComMensagem() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(post("/admin/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/delete do próprio usuário redireciona com erro")
    void delete_proprioUsuario_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Não é possível excluir o próprio usuário autenticado"))
                .when(userService).delete(1L);

        mockMvc.perform(post("/admin/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
