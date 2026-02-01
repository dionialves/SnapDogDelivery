package com.dionialves.snapdogdelivery.client.dto;

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
    private String address;

    public static ClientResponseDTO fromEntity(Client client) {
        return new ClientResponseDTO(client.getId(), client.getName(), client.getAddress());
    }

}
