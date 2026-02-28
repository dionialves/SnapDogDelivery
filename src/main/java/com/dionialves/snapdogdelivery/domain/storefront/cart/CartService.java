package com.dionialves.snapdogdelivery.domain.storefront.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dionialves.snapdogdelivery.domain.admin.product.ProductService;
import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia o {@link Cart} armazenado na sessão HTTP.
 * Chave de sessão: "cart".
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final String SESSION_KEY = "cart";
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final ProductService productService;

    /**
     * Retorna o carrinho da sessão, criando um novo se não existir.
     */
    public Cart getCart(HttpSession session) {
        var cart = (Cart) session.getAttribute(SESSION_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(SESSION_KEY, cart);
        }
        return cart;
    }

    /**
     * Adiciona um produto ao carrinho.
     * Consulta o ProductService para obter nome, preço e imageUrl.
     *
     * @throws NotFoundException    se o produto não existir
     * @throws BusinessException    se o produto estiver inativo
     */
    public void addItem(HttpSession session, Long productId, int quantity) {
        var product = productService.findById(productId);

        if (!product.isActive()) {
            throw new BusinessException("Produto indisponível no momento.");
        }

        var cart = getCart(session);
        cart.addItem(productId, product.getName(), product.getImageUrl(), product.getPrice(), quantity);
        session.setAttribute(SESSION_KEY, cart);

        log.info("Item adicionado ao carrinho: produto={}, qty={}", productId, quantity);
    }

    /**
     * Remove um item do carrinho pelo id do produto.
     */
    public void removeItem(HttpSession session, Long productId) {
        var cart = getCart(session);
        cart.removeItem(productId);
        session.setAttribute(SESSION_KEY, cart);
    }

    /**
     * Atualiza a quantidade de um item.
     * Quantidade zero ou negativa remove o item.
     */
    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        var cart = getCart(session);
        cart.updateQuantity(productId, quantity);
        session.setAttribute(SESSION_KEY, cart);
    }

    /**
     * Esvazia o carrinho.
     */
    public void clear(HttpSession session) {
        var cart = getCart(session);
        cart.clear();
        session.setAttribute(SESSION_KEY, cart);
    }

    /**
     * Retorna a quantidade total de itens no carrinho (para o contador do header).
     * Retorna 0 se não houver carrinho na sessão.
     */
    public int getItemCount(HttpSession session) {
        var cart = (Cart) session.getAttribute(SESSION_KEY);
        return cart == null ? 0 : cart.getTotalItems();
    }
}
