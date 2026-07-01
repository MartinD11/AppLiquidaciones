package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.Service.AuctionService;
import com.liquidacionremates.app.Service.ClientService;
import com.liquidacionremates.app.Service.ProductService;
import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final ProductService productService;
    private final ClientService clientService;

    // =====================================================================================
    // 1. DASHBOARD DE REMATES (Creación y Listado)
    // =====================================================================================

    @GetMapping
    public String listAuctions(@RequestParam(required = false) LocalDate searchDate, Model model) {
        List<AuctionDTO> auctions;

        try {
            if (searchDate != null) {
                // Si buscó por fecha, traemos solo ese
                auctions = List.of(auctionService.findByDate(searchDate));
            } else {
                // Si no buscó nada, traemos todos
                auctions = auctionService.findAll();
            }
        } catch (ResourceNotFoundException e) {
            // Si la fecha no existe, mandamos lista vacía y un mensaje
            auctions = List.of();
            model.addAttribute("error", "No hay remates programados para esa fecha.");
        }

        model.addAttribute("auctions", auctions);
        model.addAttribute("newAuction", new AuctionDTO());
        return "auctions/dashboard";
    }

    @PostMapping("/save")
    public String saveAuction(@ModelAttribute("newAuction") AuctionDTO auctionDTO) {
            auctionService.save(auctionDTO);
            return "redirect:/auctions";

    }

    @PostMapping("/update/{id}")
    public String updateAuction(@PathVariable Long id, @ModelAttribute AuctionDTO auctionDTO) {
            auctionService.update(id, auctionDTO);
            return "redirect:/auctions";

    }

    @GetMapping("/delete/{id}")
    public String deleteAuction(@PathVariable Long id) {
            auctionService.delete(id);
            return "redirect:/auctions";

    }


    @GetMapping("/{id}/catalog")
    public String showAuctionCatalog(@PathVariable Long id, Model model) {
            AuctionDTO auction = auctionService.findById(id);
            List<ProductDTO> availableProducts = productService.findAvailableProducts();

            model.addAttribute("auction", auction);
            model.addAttribute("availableProducts", availableProducts);
            model.addAttribute("clients", clientService.findAll());

            return "auctions/catalog";

    }

    @PostMapping("/{id}/add-products")
    public String addProducts(@PathVariable Long id, @RequestParam(required = false) List<Long> productIds) {
            if (productIds != null && !productIds.isEmpty()) {
                auctionService.addProductsToAuction(id, productIds);
            }
            return "redirect:/auctions/" + id + "/catalog";

    }

    @GetMapping("/{id}/remove-product/{productId}")
    public String removeProduct(@PathVariable Long id, @PathVariable Long productId) {
            auctionService.removeProductFromAuction(productId);
            return "redirect:/auctions/" + id + "/catalog";
    }
}