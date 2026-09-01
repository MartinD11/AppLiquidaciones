package com.liquidacionremates.app.controller;

import com.liquidacionremates.app.Service.AuctionService;
import com.liquidacionremates.app.Service.ClientService;
import com.liquidacionremates.app.Service.LiquidationService;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.entity.Liquidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/liquidations")
@RequiredArgsConstructor
public class LiquidationController {
    private final LiquidationService liquidationService;
    private final AuctionService auctionService;
    private final ClientService clientService;

    @GetMapping
    public String showDashboard(Model model,
                                @RequestParam(required = false) Long auctionId,
                                @RequestParam(required = false) Long clientId) {

        model.addAttribute("auctions", auctionService.findAllHistorical());
        model.addAttribute("clients", clientService.findAll());

        if (auctionId != null) {
            model.addAttribute("selectedAuctionId", auctionId);
            model.addAttribute("selectedClientId", clientId);

            model.addAttribute("hasGenerated", liquidationService.hasLiquidationsForAuction(auctionId));
            model.addAttribute("liquidations", liquidationService.getFilteredLiquidations(auctionId, clientId));
            model.addAttribute("summary", liquidationService.getSummaryByAuctionAndClient(auctionId, clientId));
        }
        return "liquidations/dashboard";
    }

    @PostMapping("/generate")
    public String generateLiquidations(@RequestParam Long auctionId, RedirectAttributes redirectAttributes) {
        try {
            liquidationService.generateLiquidationsForAuction(auctionId);
            redirectAttributes.addFlashAttribute("success", "¡Liquidaciones generadas con éxito!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/liquidations?auctionId=" + auctionId;
    }

    @PostMapping("/pay/{id}")
    public String markAsPaid(@PathVariable Long id, @RequestParam Long auctionId, RedirectAttributes redirectAttributes) {
        try {
            liquidationService.markAsPaid(id);
            redirectAttributes.addFlashAttribute("success", "¡Liquidación marcada como PAGADA!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/liquidations?auctionId=" + auctionId;
    }

    @GetMapping("/view/{id}")
    public String viewLiquidationDetails(@PathVariable Long id, Model model) {
        LiquidationDTO liquidation = liquidationService.findById(id);
        model.addAttribute("liq", liquidation);
        return "liquidations/detail";
    }


}
