package com.dionialves.snapdogdelivery.order;

import lombok.Getter;

/**
 * Indica a origem do pedido: criado pelo admin manualmente ou pelo cliente via área pública.
 */
@Getter
public enum OrderOrigin {

    MANUAL("Manual"),
    ONLINE("Online");

    private final String label;

    OrderOrigin(String label) {
        this.label = label;
    }
}
