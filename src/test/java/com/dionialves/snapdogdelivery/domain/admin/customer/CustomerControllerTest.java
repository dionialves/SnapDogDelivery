package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dionialves.snapdogdelivery.domain.admin.customer.dto.CustomerDTO;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    @DisplayName("GET /admin/api/customers/search retorna lista de clientes")
    void search_retornaListaClientes() throws Exception {
        var dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("João Silva");
        dto.setPhone("(11) 91234-5678");
        dto.setEmail("joao@email.com");
        dto.setCreatedAt(LocalDate.now());

        when(customerService.search("joão")).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/api/customers/search").param("q", "joão"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"));
    }

    @Test
    @DisplayName("GET /admin/api/customers/search com termo vazio retorna todos")
    void search_termoVazio_retornaTodos() throws Exception {
        var dto1 = new CustomerDTO();
        dto1.setId(1L);
        dto1.setName("João Silva");

        var dto2 = new CustomerDTO();
        dto2.setId(2L);
        dto2.setName("Maria Santos");

        when(customerService.search("")).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/admin/api/customers/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /admin/api/customers/search sem resultados retorna lista vazia")
    void search_semResultados_retornaListaVazia() throws Exception {
        when(customerService.search("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/customers/search").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
