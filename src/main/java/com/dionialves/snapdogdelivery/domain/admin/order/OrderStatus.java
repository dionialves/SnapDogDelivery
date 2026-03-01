package com.dionialves.snapdogdelivery.domain.admin.order;

public enum OrderStatus {
    PENDING("Aguardando"),
    PREPARING("Em preparo"),
    OUT_FOR_DELIVERY("Saiu para entrega"),
    DELIVERED("Entregue"),
    CANCELED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> PREPARING;
            case PREPARING -> OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> DELIVERED;
            default -> null;
        };
    }

    public boolean canAdvanceTo(OrderStatus newStatus) {
        OrderStatus next = this.getNextStatus();
        return next != null && next == newStatus;
    }

    public boolean canCancel() {
        return this == PENDING;
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELED;
    }
}
