package com.dionialves.snapdogdelivery.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dionialves.snapdogdelivery.client.dto.ClientResponseDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("admin/clients")
public class ClientViewController {

    private final ClientService clientService;

    @GetMapping
    public String findAll(Model model) {

        model.addAttribute("activeMenu", "clientes");
        model.addAttribute("pageTitle", "Clientes");
        model.addAttribute("pageSubtitle", "Gerencie os clientes cadastrados");

        List<ClientResponseDTO> clients = clientService.findAll();
        model.addAttribute("clients", clients);

        return "admin/clients/list";
    }
}
