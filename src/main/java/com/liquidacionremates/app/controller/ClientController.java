package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.dto.ClientDTO;
import org.springframework.ui.Model;
import com.liquidacionremates.app.Service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public String listClients(Model model) {
        var clients = clientService.findAll();
        model.addAttribute("clients", clients);
        return "clients/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("client", new ClientDTO());
        return "clients/form";
    }

    @PostMapping("/save")
    public String saveClient(@ModelAttribute("client") ClientDTO clientDTO) {
        clientService.save(clientDTO);
        return "redirect:/clients/new";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ClientDTO clientDTO = clientService.findById(id);
        model.addAttribute("client", clientDTO);
        return "clients/edit";
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
        return clientService.save(clientDTO);
    }

    @GetMapping("/search")
    @ResponseBody
    public List<ClientDTO> searchClients(@RequestParam("q") String query) {
        return clientService.searchByNameOrLastName(query);
    }

    @GetMapping("/search-historical")
    @ResponseBody
    public List<ClientDTO> searchHistoricalClients(@RequestParam("q") String query) {
        return clientService.searchHistorical(query);
    }
}