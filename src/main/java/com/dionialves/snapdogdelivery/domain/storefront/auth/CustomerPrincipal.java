package com.dionialves.snapdogdelivery.domain.storefront.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;

/**
 * Principal do cliente autenticado na área pública.
 * Separa username (e-mail, usado pelo Spring Security) do nome de exibição
 * (nome real, acessível via sec:authentication="principal.name" nos templates).
 */
public class CustomerPrincipal implements UserDetails {

    private final Customer customer;

    public CustomerPrincipal(Customer customer) {
        this.customer = customer;
    }

    /** Nome de exibição — acessado via sec:authentication="principal.name" */
    public String getName() {
        return customer.getName();
    }

    /** E-mail usado pelo Spring Security para autenticação */
    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public boolean isEnabled() {
        return customer.isActive();
    }
}
