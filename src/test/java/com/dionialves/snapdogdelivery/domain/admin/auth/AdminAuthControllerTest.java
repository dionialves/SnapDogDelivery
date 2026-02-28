package com.dionialves.snapdogdelivery.domain.admin.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @InjectMocks
    private AdminAuthController adminAuthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET /admin/login retorna status 200 e renderiza template admin/auth/login")
    void loginPage_semParametros_retornaTemplate() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/login"));
    }

    @Test
    @DisplayName("GET /admin/login?error=true adiciona mensagem de erro ao model")
    void loginPage_comErro_adicionaMensagemNoModel() throws Exception {
        mockMvc.perform(get("/admin/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /admin/login?logout=true adiciona mensagem de sucesso ao model")
    void loginPage_comLogout_adicionaMensagemNoModel() throws Exception {
        mockMvc.perform(get("/admin/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/login"))
                .andExpect(model().attributeExists("message"));
    }
}
