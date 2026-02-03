package com.dionialves.snapdogdelivery.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateDTO {

    @NotEmpty(message = "Name is mandatory")
    private String name;

    @NotEmpty(message = "Phone is mandatory")
    private String phone;

    @NotEmpty(message = "Email is mandatory")
    @Email
    private String email;

    @NotEmpty(message = "City is mandatory")
    private String city;

    @NotEmpty(message = "State is mandatory")
    private String state;

    @NotEmpty(message = "Address is mandatory")
    private String address;

}
