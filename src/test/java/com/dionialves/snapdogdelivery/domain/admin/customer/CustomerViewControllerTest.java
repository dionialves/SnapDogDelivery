package com.dionialves.snapdogdelivery.domain.admin.customer;

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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

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
class CustomerViewControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerViewController customerViewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(customerViewController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();
    }

    // ---------- GET /admin/customers ----------

    @Test
    @DisplayName("GET /admin/customers retorna lista com paginação no model")
    void findAll_retornaListaComPaginacao() throws Exception {
        var dto = buildCustomerDTO(1L, "João Silva");
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(customerService.search(any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers/list"))
                .andExpect(model().attributeExists("customers", "currentPage", "totalPages", "totalElements"));
    }

    // ---------- GET /admin/customers/new ----------

    @Test
    @DisplayName("GET /admin/customers/new retorna formulário com CustomerDTO vazio")
    void newCustomer_retornaFormularioVazio() throws Exception {
        mockMvc.perform(get("/admin/customers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers/form"))
                .andExpect(model().attributeExists("customer"));
    }

    // ---------- GET /admin/customers/{id} ----------

    @Test
    @DisplayName("GET /admin/customers/{id} retorna formulário com dados do cliente")
    void findById_retornaFormularioPreenchido() throws Exception {
        var dto = buildCustomerDTO(5L, "Maria Santos");
        when(customerService.findById(5L)).thenReturn(dto);

        mockMvc.perform(get("/admin/customers/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers/form"))
                .andExpect(model().attribute("customer", dto));
    }

    // ---------- POST /admin/customers/new ----------

    @Test
    @DisplayName("POST /admin/customers/new com dados válidos cria cliente e redireciona com sucesso")
    void saved_dadosValidos_redirecionaComSucesso() throws Exception {
        when(customerService.create(any())).thenReturn(buildCustomerDTO(10L, "Novo Cliente"));

        mockMvc.perform(post("/admin/customers/new")
                .param("name", "Novo Cliente")
                .param("phone", "(11) 91234-5678")
                .param("email", "novo@email.com")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("neighborhood", "Centro")
                .param("street", "Rua das Flores")
                .param("zipCode", "01310-100")
                .param("number", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attribute("successMessage", "Cliente criado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/customers/new com campos inválidos retorna formulário com erros")
    void saved_camposInvalidos_retornaFormulario() throws Exception {
        mockMvc.perform(post("/admin/customers/new")
                .param("name", "")     // nome vazio — inválido
                .param("email", ""))   // e-mail vazio — inválido
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers/form"));
    }

    // ---------- POST /admin/customers/{id} ----------

    @Test
    @DisplayName("POST /admin/customers/{id} com dados válidos atualiza e redireciona")
    void update_dadosValidos_redirecionaComSucesso() throws Exception {
        when(customerService.update(eq(1L), any())).thenReturn(buildCustomerDTO(1L, "João Atualizado"));

        mockMvc.perform(post("/admin/customers/1")
                .param("name", "João Atualizado")
                .param("phone", "(11) 91234-5678")
                .param("email", "joao@email.com")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("neighborhood", "Centro")
                .param("street", "Rua das Flores")
                .param("zipCode", "01310-100")
                .param("number", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attribute("successMessage", "Cliente atualizado com sucesso!"));
    }

    // ---------- POST /admin/customers/{id}/delete ----------

    @Test
    @DisplayName("POST /admin/customers/{id}/delete exclui cliente e redireciona com sucesso")
    void delete_clienteExistente_redirecionaComSucesso() throws Exception {
        doNothing().when(customerService).delete(3L);

        mockMvc.perform(post("/admin/customers/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attribute("successMessage", "Cliente deletado com sucesso!"));
    }

    @Test
    @DisplayName("POST /admin/customers/{id}/delete com NotFoundException redireciona com errorMessage")
    void delete_clienteInexistente_redirecionaComErro() throws Exception {
        doThrow(new NotFoundException("Cliente não encontrado com ID: 99"))
                .when(customerService).delete(99L);

        mockMvc.perform(post("/admin/customers/99/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- Auxiliares ----------

    private CustomerDTO buildCustomerDTO(Long id, String name) {
        var dto = new CustomerDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(name.toLowerCase().replace(" ", "") + "@email.com");
        dto.setPhone("(11) 91234-5678");
        dto.setCity("São Paulo");
        dto.setState(State.SP);
        dto.setNeighborhood("Centro");
        dto.setStreet("Rua Teste");
        dto.setZipCode("01310-100");
        dto.setNumber("1");
        dto.setActive(true);
        return dto;
    }
}
