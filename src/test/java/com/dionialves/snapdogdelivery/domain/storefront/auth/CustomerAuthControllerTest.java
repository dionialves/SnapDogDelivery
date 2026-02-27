package com.dionialves.snapdogdelivery.domain.storefront.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CustomerAuthControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerAuthController customerAuthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(customerAuthController)
                .setViewResolvers(viewResolver)
                .build();
    }

    // ---------- GET /login ----------

    @Test
    @DisplayName("GET /login sem parâmetros retorna view de login sem atributos extras")
    void loginPage_semParametros_retornaViewLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/auth/login"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    @DisplayName("GET /login?error=true adiciona atributo de erro ao model")
    void loginPage_comErro_adicionaAtributoErro() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    // ---------- GET /register ----------

    @Test
    @DisplayName("GET /register retorna formulário de cadastro com registerDTO no model")
    void registerPage_retornaFormularioComDTO() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/auth/register"))
                .andExpect(model().attributeExists("registerDTO", "states"));
    }

    // ---------- POST /register ----------

    @Test
    @DisplayName("POST /register com dados válidos salva o cliente e redireciona para /login")
    void register_dadosValidos_redirecionaParaLogin() throws Exception {
        when(customerRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");

        mockMvc.perform(post("/register")
                .param("name", "Novo Cliente")
                .param("email", "novo@email.com")
                .param("password", "senha123")
                .param("phone", "(11) 91234-5678")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("neighborhood", "Centro")
                .param("street", "Rua das Flores")
                .param("zipCode", "01310-100")
                .param("number", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(customerRepository).save(any());
    }

    @Test
    @DisplayName("POST /register com e-mail duplicado retorna formulário com errorMessage")
    void register_emailDuplicado_retornaFormularioComErro() throws Exception {
        when(customerRepository.existsByEmail("existente@email.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                .param("name", "Cliente Existente")
                .param("email", "existente@email.com")
                .param("password", "senha123")
                .param("phone", "(11) 91234-5678")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("neighborhood", "Centro")
                .param("street", "Rua das Flores")
                .param("zipCode", "01310-100")
                .param("number", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
