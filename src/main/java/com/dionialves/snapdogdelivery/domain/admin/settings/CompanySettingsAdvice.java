package com.dionialves.snapdogdelivery.domain.admin.settings;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.dionialves.snapdogdelivery.domain.storefront.account.AccountController;
import com.dionialves.snapdogdelivery.domain.storefront.auth.CustomerAuthController;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartController;
import com.dionialves.snapdogdelivery.domain.storefront.checkout.CheckoutController;
import com.dionialves.snapdogdelivery.domain.storefront.store.StoreController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice(assignableTypes = {
        StoreController.class,
        CartController.class,
        CheckoutController.class,
        AccountController.class,
        CustomerAuthController.class
})
public class CompanySettingsAdvice {

    private final CompanySettingsService settingsService;

    @ModelAttribute
    public void addCompanySettings(Model model) {
        model.addAttribute("companySettings", settingsService.get());
    }
}
