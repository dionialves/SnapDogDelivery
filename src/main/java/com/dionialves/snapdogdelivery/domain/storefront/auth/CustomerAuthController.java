package com.dionialves.snapdogdelivery.domain.storefront.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.storefront.auth.dto.CustomerRegisterDTO;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerAuthController {

    private static final Logger log = LoggerFactory.getLogger(CustomerAuthController.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Exibe o formulário de login público do cliente.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "E-mail ou senha inválidos.");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Você saiu com sucesso.");
        }

        return "public/auth/login";
    }

    /**
     * Exibe o formulário de cadastro do cliente.
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new CustomerRegisterDTO());
        model.addAttribute("states", State.values());
        return "public/auth/register";
    }

    /**
     * Processa o cadastro do cliente:
     * 1. Valida os dados do formulário
     * 2. Verifica unicidade do e-mail na tabela de clientes
     * 3. Cria o Customer com credenciais próprias (password + active)
     * 4. Redireciona para /login com mensagem de sucesso
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDTO") CustomerRegisterDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("states", State.values());
            return "public/auth/register";
        }

        try {
            if (customerRepository.existsByEmail(dto.getEmail())) {
                throw new BusinessException("Já existe uma conta com o e-mail: " + dto.getEmail());
            }

            var customer = new Customer();
            customer.setName(dto.getName());
            customer.setPhone(dto.getPhone());
            customer.setEmail(dto.getEmail());
            customer.setCity(dto.getCity());
            customer.setState(dto.getState());
            customer.setNeighborhood(dto.getNeighborhood());
            customer.setStreet(dto.getStreet());
            customer.setZipCode(dto.getZipCode());
            customer.setNumber(dto.getNumber());
            customer.setComplement(dto.getComplement());
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
            customer.setActive(true);

            customerRepository.save(customer);

            log.info("Novo cliente cadastrado: {}", dto.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("states", State.values());
            return "public/auth/register";
        }
    }
}
