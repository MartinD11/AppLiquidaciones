package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.dto.ClientDTO;
import org.springframework.ui.Model;
import com.liquidacionremates.app.Service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public String listClients(Model model) {
        var clients = clientService.findAll();
        model.addAttribute("clients", clients);
        return "clients/list"; // <-- Bien, sin barra
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        System.out.println("¡BINGO! El clic llegó al controlador Java.");
        model.addAttribute("client", new ClientDTO());
        return "clients/form"; //
    }

    @PostMapping("/save")
    public String saveClient(@ModelAttribute("client") ClientDTO clientDTO) {
        clientService.save(clientDTO);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ClientDTO clientDTO = clientService.findById(id);
        model.addAttribute("client", clientDTO);
        return "clients/edit"; // <-- CORREGIDO (sin barra inicial)
    }

    @PostMapping("/update/{id}")
    public String updateClient(@PathVariable Long id, @ModelAttribute("client") ClientDTO clientDTO) {
        clientService.update(id, clientDTO);
        return "redirect:/clients";
    }

    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return "redirect:/clients";
    }

    @PostMapping("/save-ajax")
    @ResponseBody
    public ClientDTO saveClientAjax(@RequestBody ClientDTO clientDTO) {
        // Usas tu servicio para guardar y devolver el objeto con el ID generado
        return clientService.save(clientDTO);
    }
}