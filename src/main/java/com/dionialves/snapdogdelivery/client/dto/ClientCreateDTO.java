package com.dionialves.snapdogdelivery.client.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDTO {

    @NotEmpty(message = "Name is mandatory")
    private String name;

    @NotEmpty(message = "Address is mandatory")
    private String address;

}
