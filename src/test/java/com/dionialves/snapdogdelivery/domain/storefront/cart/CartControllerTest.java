package com.dionialves.snapdogdelivery.domain.storefront.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dionialves.snapdogdelivery.exception.BusinessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setViewResolvers(viewResolver)
                .build();

        session = new MockHttpSession();
    }

    // ---------- GET /cart ----------

    @Test
    @DisplayName("GET /cart exibe o carrinho com atributos corretos no model")
    void viewCart_retornaViewComCarrinho() throws Exception {
        var cart = new Cart();
        when(cartService.getCart(any())).thenReturn(cart);
        when(cartService.getItemCount(any())).thenReturn(0);

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("public/cart/cart"))
                .andExpect(model().attributeExists("cart", "cartItemCount"));
    }

    // ---------- POST /cart/add ----------

    @Test
    @DisplayName("POST /cart/add com produto válido adiciona ao carrinho e redireciona para /catalog")
    void addItem_produtoValido_redirecionaParaCatalogo() throws Exception {
        mockMvc.perform(post("/cart/add").session(session)
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"))
                .andExpect(flash().attribute("successMessage", "Produto adicionado ao carrinho!"));

        verify(cartService).addItem(any(), eq(1L), eq(2));
    }

    @Test
    @DisplayName("POST /cart/add com BusinessException redireciona para /catalog com errorMessage")
    void addItem_businessException_redirecionaComErro() throws Exception {
        doThrow(new BusinessException("Produto indisponível no momento."))
                .when(cartService).addItem(any(), eq(99L), eq(1));

        mockMvc.perform(post("/cart/add").session(session)
                .param("productId", "99")
                .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"))
                .andExpect(flash().attribute("errorMessage", "Produto indisponível no momento."));
    }

    // ---------- POST /cart/remove/{productId} ----------

    @Test
    @DisplayName("POST /cart/remove/{id} remove o item e redireciona para /cart")
    void removeItem_redirecionaParaCarrinho() throws Exception {
        mockMvc.perform(post("/cart/remove/1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("successMessage", "Item removido do carrinho."));

        verify(cartService).removeItem(any(), eq(1L));
    }

    // ---------- POST /cart/update/{productId} ----------

    @Test
    @DisplayName("POST /cart/update/{id} atualiza quantidade e redireciona para /cart")
    void updateItem_redirecionaParaCarrinho() throws Exception {
        mockMvc.perform(post("/cart/update/1").session(session)
                .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateQuantity(any(), eq(1L), eq(3));
    }

    // ---------- POST /cart/clear ----------

    @Test
    @DisplayName("POST /cart/clear esvazia o carrinho e redireciona para /cart")
    void clearCart_redirecionaParaCarrinho() throws Exception {
        mockMvc.perform(post("/cart/clear").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("successMessage", "Carrinho esvaziado."));

        verify(cartService).clear(any());
    }
}
