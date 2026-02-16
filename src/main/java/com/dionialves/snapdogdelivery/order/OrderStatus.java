package com.dionialves.snapdogdelivery.order;

public enum OrderStatus {
    PENDING,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELED;

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
