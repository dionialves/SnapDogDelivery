package com.dionialves.snapdogdelivery.domain.storefront.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Carrinho de compras do cliente.
 * POJO Serializable — armazenado na sessão HTTP com a chave "cart".
 * Não é uma entidade JPA; não é persistido em banco.
 */
@Getter
@NoArgsConstructor
public class Cart implements Serializable {

    /** Itens do carrinho indexados por productId. Mantém ordem de inserção. */
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    /**
     * Adiciona um produto ao carrinho.
     * Se já existir, incrementa a quantidade.
     */
    public void addItem(Long productId, String productName, String imageUrl, BigDecimal unitPrice, int quantity) {
        if (items.containsKey(productId)) {
            var existing = items.get(productId);
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            items.put(productId, new CartItem(productId, productName, imageUrl, unitPrice, quantity));
        }
    }

    /**
     * Remove um item do carrinho pelo id do produto.
     */
    public void removeItem(Long productId) {
        items.remove(productId);
    }

    /**
     * Atualiza a quantidade de um item.
     * Se a quantidade for zero ou negativa, remove o item.
     */
    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeItem(productId);
        } else if (items.containsKey(productId)) {
            items.get(productId).setQuantity(quantity);
        }
    }

    /**
     * Retorna a coleção de itens do carrinho para iteração nos templates.
     */
    public Collection<CartItem> getItemList() {
        return items.values();
    }

    /**
     * Retorna o valor total do carrinho (soma dos subtotais de cada item).
     */
    public BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna a quantidade total de itens (soma das quantidades individuais).
     */
    public int getTotalItems() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Verifica se o carrinho está vazio.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Esvazia o carrinho.
     */
    public void clear() {
        items.clear();
    }
}
