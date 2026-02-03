package com.dionialves.snapdogdelivery.client.dto;

import java.time.LocalDate;

import com.dionialves.snapdogdelivery.client.Client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String city;
    private String state;
    private String address;
    private LocalDate createdAt;

    public static ClientResponseDTO fromEntity(Client client) {
        var dto = new ClientResponseDTO();

        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());
        dto.setCity(client.getCity());
        dto.setState(client.getState());
        dto.setAddress(client.getAddress());
        dto.setCreatedAt(client.getCreatedAt());

        return dto;
    }

}
