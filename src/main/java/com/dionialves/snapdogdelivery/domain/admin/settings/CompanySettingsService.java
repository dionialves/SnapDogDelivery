package com.dionialves.snapdogdelivery.domain.admin.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.domain.admin.settings.dto.CompanySettingsDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CompanySettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final CompanySettingsRepository repository;

    @Transactional(readOnly = true)
    public CompanySettings get() {
        return repository.findById(SETTINGS_ID)
                .orElseGet(this::defaultSettings);
    }

    @Transactional
    public void update(CompanySettingsDTO dto) {
        CompanySettings settings = repository.findById(SETTINGS_ID)
                .orElseGet(this::defaultSettings);

        settings.setCompanyName(dto.getCompanyName());
        settings.setEmail(dto.getEmail());
        settings.setPhone(dto.getPhone());
        settings.setAddress(dto.getAddress());
        settings.setOpeningHours(dto.getOpeningHours());
        settings.setCopyright(dto.getCopyright());

        repository.save(settings);
    }

    private CompanySettings defaultSettings() {
        var s = new CompanySettings();
        s.setCompanyName("SnapDog Delivery");
        s.setEmail("contato@snapdogdelivery.com");
        s.setPhone("(00) 12345-6789");
        s.setAddress("Rua Example, 100 — São Paulo, SP");
        s.setOpeningHours("Seg–Sex: 11h às 22h | Sáb–Dom: 11h às 23h");
        s.setCopyright("© 2026 SnapDog Delivery. Todos os direitos reservados.");
        return s;
    }
}
