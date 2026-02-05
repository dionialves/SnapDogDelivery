package com.dionialves.snapdogdelivery.client.dto;

import java.time.LocalDate;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.client.State;

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
public class ClientDTO {

    private Long id;

    @NotEmpty(message = "Name is mandatory")
    @Size(max = 100, message = "Name must be at most {max} characters")
    private String name;

    @NotEmpty(message = "Phone is mandatory")
    @Size(max = 15, message = "Phone must be at most {max} characters")
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Phone format: (00) 12345-6789")
    private String phone;

    @NotEmpty(message = "Email is mandatory")
    @Size(max = 100, message = "Email must be at most {max} characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "City is mandatory")
    @Size(max = 100, message = "City must be at most {max} characters")
    private String city;

    @NotNull(message = "State is mandatory")
    private State state;

    @NotEmpty(message = "Neighborhood is mandatory")
    @Size(max = 100, message = "Neighborhood must be at most {max} characters")
    private String neighborhood;

    @NotEmpty(message = "Street is mandatory")
    @Size(max = 100, message = "Street must be at most {max} characters")
    private String street;

    @NotEmpty(message = "Zip code is mandatory")
    @Size(max = 9, message = "Zip Code must be at most 9 characters")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Zip code format: 12345-678")
    private String zipCode;

    @NotEmpty(message = "Number is mandatory")
    @Size(max = 10, message = "Number must be at most {max} characters")
    private String number;

    @Size(max = 100, message = "Complement must be at most {max} characters")
    private String complement;
    private LocalDate createdAt;

    public static ClientDTO fromEntity(Client client) {
        var dto = new ClientDTO();

        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());
        dto.setCity(client.getCity());
        dto.setState(client.getState());
        dto.setNeighborhood(client.getNeighborhood());
        dto.setStreet(client.getStreet());
        dto.setZipCode(client.getZipCode());
        dto.setNumber(client.getNumber());
        dto.setComplement(client.getComplement());
        dto.setCreatedAt(client.getCreatedAt());

        return dto;
    }

}
