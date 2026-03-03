package com.dionialves.snapdogdelivery.domain.admin.settings.dto;

import com.dionialves.snapdogdelivery.domain.admin.settings.CompanySettings;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanySettingsDTO {

    @NotEmpty(message = "Nome da empresa é obrigatório")
    private String companyName;

    @NotEmpty(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotEmpty(message = "Telefone é obrigatório")
    private String phone;

    @NotEmpty(message = "Endereço é obrigatório")
    private String address;

    @NotEmpty(message = "Horário de funcionamento é obrigatório")
    private String openingHours;

    @NotEmpty(message = "Copyright é obrigatório")
    private String copyright;

    public static CompanySettingsDTO fromEntity(CompanySettings s) {
        var dto = new CompanySettingsDTO();
        dto.setCompanyName(s.getCompanyName());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setAddress(s.getAddress());
        dto.setOpeningHours(s.getOpeningHours());
        dto.setCopyright(s.getCopyright());
        return dto;
    }
}
