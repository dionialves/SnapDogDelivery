package com.dionialves.snapdogdelivery.user.dto;

import com.dionialves.snapdogdelivery.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {

    @NotEmpty(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo {max} caracteres")
    private String name;

    @NotEmpty(message = "E-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 100, message = "E-mail deve ter no máximo {max} caracteres")
    private String email;

    @NotEmpty(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre {min} e {max} caracteres")
    private String password;

    @NotNull(message = "Perfil é obrigatório")
    private Role role;
}
