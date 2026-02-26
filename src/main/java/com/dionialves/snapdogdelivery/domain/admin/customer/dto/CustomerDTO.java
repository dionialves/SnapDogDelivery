package com.dionialves.snapdogdelivery.domain.admin.customer.dto;

import java.time.LocalDate;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotEmpty(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo {max} caracteres")
    private String name;

    @NotEmpty(message = "Telefone é obrigatório")
    @Size(max = 15, message = "Telefone deve ter no máximo {max} caracteres")
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Formato do telefone: (00) 12345-6789")
    private String phone;

    @NotEmpty(message = "E-mail é obrigatório")
    @Size(max = 100, message = "E-mail deve ter no máximo {max} caracteres")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotEmpty(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo {max} caracteres")
    private String city;

    @NotNull(message = "Estado é obrigatório")
    private State state;

    @NotEmpty(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo {max} caracteres")
    private String neighborhood;

    @NotEmpty(message = "Rua é obrigatória")
    @Size(max = 100, message = "Rua deve ter no máximo {max} caracteres")
    private String street;

    @NotEmpty(message = "CEP é obrigatório")
    @Size(max = 9, message = "CEP deve ter no máximo 9 caracteres")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Formato do CEP: 12345-678")
    private String zipCode;

    @NotEmpty(message = "Número é obrigatório")
    @Size(max = 10, message = "Número deve ter no máximo {max} caracteres")
    private String number;

    @Size(max = 100, message = "Complemento deve ter no máximo {max} caracteres")
    private String complement;
    private LocalDate createdAt;

    public static CustomerDTO fromEntity(Customer customer) {
        var dto = new CustomerDTO();

        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setNeighborhood(customer.getNeighborhood());
        dto.setStreet(customer.getStreet());
        dto.setZipCode(customer.getZipCode());
        dto.setNumber(customer.getNumber());
        dto.setComplement(customer.getComplement());
        dto.setCreatedAt(customer.getCreatedAt());

        return dto;
    }

}
