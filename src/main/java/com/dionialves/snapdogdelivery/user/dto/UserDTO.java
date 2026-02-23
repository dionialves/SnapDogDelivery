package com.dionialves.snapdogdelivery.user.dto;

import java.time.LocalDateTime;

import com.dionialves.snapdogdelivery.user.Role;
import com.dionialves.snapdogdelivery.user.User;

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
public class UserDTO {

    private Long id;

    @NotEmpty(message = "Name is mandatory")
    @Size(max = 100, message = "Name must be at most {max} characters")
    private String name;

    @NotEmpty(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most {max} characters")
    private String email;

    @NotNull(message = "Role is mandatory")
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDTO fromEntity(User user) {
        var dto = new UserDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }
}