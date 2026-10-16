package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.Service.AuctionService;
import com.liquidacionremates.app.Service.BuyerLiquidationService;
import com.liquidacionremates.app.Service.ClientService;
import com.liquidacionremates.app.dto.BuyerLiquidationSummaryDTO; // Importación necesaria
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/buyers")
@RequiredArgsConstructor
public class BuyerLiquidationController {

    private final BuyerLiquidationService buyerLiquidationService;
    private final AuctionService auctionService;
    private final ClientService clientService;

    @GetMapping
    public String listBuyers(@RequestParam(required = false) Long auctionId,
                             @RequestParam(required = false) Long buyerId,
                             Model model) {

        model.addAttribute("auctions", auctionService.findAll());
        model.addAttribute("clients", clientService.findAll());

        if (auctionId != null) {
            var buyerLiquidations = buyerLiquidationService.getLiquidations(auctionId, buyerId);
            model.addAttribute("buyerLiquidations", buyerLiquidations);
            model.addAttribute("selectedAuctionId", auctionId);
            model.addAttribute("selectedClientId", buyerId);
            BuyerLiquidationSummaryDTO summary = buyerLiquidationService.getSummary(buyerLiquidations);
            model.addAttribute("summary", summary);
        }

        return "buyers/dashboard";
    }

    @PostMapping("/generate")
    public String generateLiquidations(@RequestParam Long auctionId, RedirectAttributes redirectAttributes) {
        try {
            buyerLiquidationService.generateOrUpdateForAuction(auctionId);
            redirectAttributes.addFlashAttribute("success", "Cobranzas generadas/actualizadas con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar cobranzas: " + e.getMessage());
        }
        return "redirect:/buyers?auctionId=" + auctionId;
    }

    @PostMapping("/pay/{id}")
    public String markAsPaid(@PathVariable Long id, @RequestParam Long auctionId, RedirectAttributes redirectAttributes) {
        buyerLiquidationService.markAsPaid(id);
        redirectAttributes.addFlashAttribute("success", "Cobro marcado como PAGADO.");
        return "redirect:/buyers?auctionId=" + auctionId;
    }
}