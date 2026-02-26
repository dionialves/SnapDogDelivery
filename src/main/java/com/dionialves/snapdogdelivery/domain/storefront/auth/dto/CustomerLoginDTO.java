package com.dionialves.snapdogdelivery.domain.storefront.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerLoginDTO {

    @NotEmpty(message = "E-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotEmpty(message = "Senha é obrigatória")
    private String password;
}
