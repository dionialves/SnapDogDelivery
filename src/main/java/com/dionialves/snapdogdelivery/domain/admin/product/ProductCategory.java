package com.dionialves.snapdogdelivery.domain.admin.product;

public enum ProductCategory {

    HOT_DOG("Hot Dog"),
    BEBIDA("Bebida");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
