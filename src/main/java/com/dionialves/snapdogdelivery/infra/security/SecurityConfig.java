package com.dionialves.snapdogdelivery.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.dionialves.snapdogdelivery.domain.storefront.auth.CustomerUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cadeia de segurança para a área administrativa (/admin/**).
     * Usa exclusivamente o CustomUserDetailsService (tb_users).
     * Ordem 1 — avaliada antes da cadeia pública.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(
            HttpSecurity http,
            CustomUserDetailsService adminUserDetailsService) throws Exception {

        http
            .securityMatcher("/admin/**")
            .authenticationProvider(adminAuthProvider(adminUserDetailsService))
            .authorizeHttpRequests(auth -> auth
                // Gerenciamento de usuários: apenas SUPER_ADMIN
                .requestMatchers("/admin/users/**").hasRole("SUPER_ADMIN")
                // Demais rotas admin: ADMIN ou SUPER_ADMIN
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Cadeia de segurança para a área pública (/, /catalog/**, /cart/**, etc.).
     * Usa exclusivamente o CustomerUserDetailsService (tb_customers).
     * Ordem 2 — avaliada após a cadeia admin.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(
            HttpSecurity http,
            CustomerUserDetailsService customerUserDetailsService) throws Exception {

        http
            .authenticationProvider(customerAuthProvider(customerUserDetailsService))
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas (sem autenticação)
                .requestMatchers("/", "/catalog", "/catalog/**", "/register", "/login", "/error",
                        "/style/**", "/js/**", "/favicon.ico", "/uploads/**").permitAll()
                // Rotas exclusivas para clientes autenticados
                .requestMatchers("/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/checkout/**").hasRole("CUSTOMER")
                .requestMatchers("/account/**").hasRole("CUSTOMER")
                // Qualquer outra rota não coberta: permitir
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/catalog", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    private AuthenticationProvider adminAuthProvider(CustomUserDetailsService service) {
        var provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    private AuthenticationProvider customerAuthProvider(CustomerUserDetailsService service) {
        var provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
