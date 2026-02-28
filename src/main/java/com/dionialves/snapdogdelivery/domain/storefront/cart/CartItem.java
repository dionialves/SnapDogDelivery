package com.dionialves.snapdogdelivery.domain.storefront.cart;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um item dentro do carrinho de compras.
 * POJO Serializable — armazenado dentro de {@link Cart} na sessão HTTP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;

    /**
     * Retorna o subtotal deste item (preço unitário × quantidade).
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
