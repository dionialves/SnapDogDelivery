package com.dionialves.snapdogdelivery.domain.storefront.auth;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;

import lombok.RequiredArgsConstructor;

/**
 * Serviço de autenticação exclusivo para clientes da área pública.
 * Consulta apenas a tabela tb_customers — independente do fluxo admin.
 */
@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cliente não encontrado com e-mail: " + email));

        if (!customer.isActive()) {
            throw new DisabledException("Conta desabilitada para o e-mail: " + email);
        }

        return new CustomerPrincipal(customer);
    }
}
