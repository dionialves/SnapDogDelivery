package com.dionialves.snapdogdelivery.user.dto;

import com.dionialves.snapdogdelivery.user.Role;

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
public class UserUpdateDTO {

    @NotEmpty(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo {max} caracteres")
    private String name;

    @NotNull(message = "Perfil é obrigatório")
    private Role role;

    /**
     * Senha opcional — somente atualiza se preenchida.
     * Quando nula ou vazia, mantém a senha atual.
     */
    @Size(min = 6, max = 100, message = "Senha deve ter entre {min} e {max} caracteres")
    private String password;
}
