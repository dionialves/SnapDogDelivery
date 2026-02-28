package com.dionialves.snapdogdelivery.domain.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    @DisplayName("Role não contém CUSTOMER — autenticação de cliente é independente")
    void role_naoContemCustomer() {
        var roles = Role.values();
        for (var role : roles) {
            assertThat(role.name()).isNotEqualTo("CUSTOMER");
        }
    }

    @Test
    @DisplayName("Role contém apenas USER, ADMIN e SUPER_ADMIN")
    void role_contemApenasRolesAdmin() {
        assertThat(Role.values()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN, Role.SUPER_ADMIN);
    }
}
