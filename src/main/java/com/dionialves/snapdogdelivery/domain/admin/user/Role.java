package com.dionialves.snapdogdelivery.domain.admin.user;

import lombok.Getter;

@Getter
public enum Role {

    USER("USER", "Usuário"),
    ADMIN("ADMIN", "Administrador"),
    SUPER_ADMIN("SUPER_ADMIN", "Super Administrador"),
    CUSTOMER("CUSTOMER", "Cliente");

    private final String code;
    private final String name;

    Role(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
