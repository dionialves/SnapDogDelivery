package com.dionialves.snapdogdelivery.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cadeia de segurança para a área administrativa (/admin/**).
     * Ordem 1 — avaliada antes da cadeia pública.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

        http
            .securityMatcher("/admin/**", "/login", "/style/**", "/js/**", "/favicon.ico")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/style/**", "/js/**", "/favicon.ico", "/error").permitAll()
                // Gerenciamento de usuários: apenas SUPER_ADMIN
                .requestMatchers("/admin/users/**").hasRole("SUPER_ADMIN")
                // Demais rotas admin: ADMIN ou SUPER_ADMIN
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Cadeia de segurança para a área pública (/, /catalog/**, /cart/**, etc.).
     * Ordem 2 — avaliada após a cadeia admin.
     * Preparada para a área pública (seção 1 do backlog): rotas de carrinho,
     * checkout e conta do cliente exigirão role CUSTOMER quando implementadas.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas (sem autenticação)
                .requestMatchers("/", "/catalog", "/catalog/**", "/register", "/error").permitAll()
                // Rotas que exigirão CUSTOMER (preparadas — não existem ainda)
                .requestMatchers("/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/checkout/**").hasRole("CUSTOMER")
                .requestMatchers("/account/**").hasRole("CUSTOMER")
                // Qualquer outra rota não coberta: permitir por ora
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
