package com.dionialves.snapdogdelivery.domain.storefront.cart;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dionialves.snapdogdelivery.domain.admin.product.dto.ProductResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private HttpSession session;

    @BeforeEach
    void setUp() {
        session = mock(HttpSession.class);
    }

    // ---------- getCart ----------

    @Test
    @DisplayName("getCart sem carrinho na sessão cria e armazena um novo Cart")
    void getCart_semCarrinho_criaNovo() {
        when(session.getAttribute("cart")).thenReturn(null);

        var cart = cartService.getCart(session);

        assertThat(cart).isNotNull();
        assertThat(cart.isEmpty()).isTrue();
        verify(session).setAttribute(eq("cart"), eq(cart));
    }

    @Test
    @DisplayName("getCart com carrinho existente retorna o mesmo Cart sem criar novo")
    void getCart_comCarrinhoExistente_retornaExistente() {
        var existing = new Cart();
        existing.addItem(1L, "Produto", null, BigDecimal.TEN, 2);
        when(session.getAttribute("cart")).thenReturn(existing);

        var cart = cartService.getCart(session);

        assertThat(cart).isSameAs(existing);
        assertThat(cart.getTotalItems()).isEqualTo(2);
    }

    // ---------- addItem ----------

    @Test
    @DisplayName("addItem com produto ativo adiciona item ao carrinho")
    void addItem_produtoAtivo_adicionaAoCarrinho() {
        var product = new ProductResponseDTO(1L, "Hot Dog", new BigDecimal("15.90"), "Delicioso", null, true, false, null, null);
        when(productService.findById(1L)).thenReturn(product);

        var cart = new Cart();
        when(session.getAttribute("cart")).thenReturn(cart);

        cartService.addItem(session, 1L, 2);

        assertThat(cart.getTotalItems()).isEqualTo(2);
        assertThat(cart.getItemList()).hasSize(1);
        verify(session).setAttribute(eq("cart"), eq(cart));
    }

    @Test
    @DisplayName("addItem com produto inativo lança BusinessException")
    void addItem_produtoInativo_lancaBusinessException() {
        var product = new ProductResponseDTO(2L, "Produto Inativo", new BigDecimal("10.00"), null, null, false, false, null, null);
        when(productService.findById(2L)).thenReturn(product);

        assertThatThrownBy(() -> cartService.addItem(session, 2L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    // ---------- removeItem ----------

    @Test
    @DisplayName("removeItem remove o produto do carrinho")
    void removeItem_produtoExistente_remove() {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, BigDecimal.TEN, 1);
        when(session.getAttribute("cart")).thenReturn(cart);

        cartService.removeItem(session, 1L);

        assertThat(cart.isEmpty()).isTrue();
        verify(session).setAttribute(eq("cart"), eq(cart));
    }

    // ---------- updateQuantity ----------

    @Test
    @DisplayName("updateQuantity com quantidade positiva atualiza o item")
    void updateQuantity_quantidadePositiva_atualiza() {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, BigDecimal.TEN, 1);
        when(session.getAttribute("cart")).thenReturn(cart);

        cartService.updateQuantity(session, 1L, 5);

        assertThat(cart.getTotalItems()).isEqualTo(5);
        verify(session).setAttribute(eq("cart"), eq(cart));
    }

    @Test
    @DisplayName("updateQuantity com quantidade zero remove o item do carrinho")
    void updateQuantity_quantidadeZero_removeItem() {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, BigDecimal.TEN, 3);
        when(session.getAttribute("cart")).thenReturn(cart);

        cartService.updateQuantity(session, 1L, 0);

        assertThat(cart.isEmpty()).isTrue();
    }

    // ---------- clear ----------

    @Test
    @DisplayName("clear esvazia o carrinho completamente")
    void clear_esvaziasCarrinho() {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, BigDecimal.TEN, 2);
        cart.addItem(2L, "Refrigerante", null, new BigDecimal("5.00"), 1);
        when(session.getAttribute("cart")).thenReturn(cart);

        cartService.clear(session);

        assertThat(cart.isEmpty()).isTrue();
        verify(session).setAttribute(eq("cart"), eq(cart));
    }

    // ---------- getItemCount ----------

    @Test
    @DisplayName("getItemCount sem carrinho na sessão retorna zero")
    void getItemCount_semCarrinho_retornaZero() {
        when(session.getAttribute("cart")).thenReturn(null);

        var count = cartService.getItemCount(session);

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("getItemCount com itens no carrinho retorna a soma das quantidades")
    void getItemCount_comItens_retornaTotal() {
        var cart = new Cart();
        cart.addItem(1L, "Hot Dog", null, BigDecimal.TEN, 3);
        cart.addItem(2L, "Refrigerante", null, new BigDecimal("5.00"), 2);
        when(session.getAttribute("cart")).thenReturn(cart);

        var count = cartService.getItemCount(session);

        assertThat(count).isEqualTo(5);
    }
}
