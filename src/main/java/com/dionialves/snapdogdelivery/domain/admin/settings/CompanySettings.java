package com.dionialves.snapdogdelivery.domain.admin.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_company_settings")
@Getter
@Setter
@NoArgsConstructor
public class CompanySettings {

    /**
     * Registro singleton — ID sempre 1.
     */
    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 200)
    private String openingHours;

    @Column(nullable = false, length = 100)
    private String copyright;
}
