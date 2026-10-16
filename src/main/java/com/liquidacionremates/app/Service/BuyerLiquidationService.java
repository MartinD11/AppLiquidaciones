package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.dto.BuyerLiquidationDTO;
import com.liquidacionremates.app.dto.BuyerLiquidationSummaryDTO;

import java.util.List;

public interface BuyerLiquidationService {
    void generateOrUpdateForAuction(Long auctionId);
    List<BuyerLiquidationDTO> getLiquidations(Long auctionId, Long buyerId);
    void markAsPaid(Long id);
    BuyerLiquidationSummaryDTO getSummary(List<BuyerLiquidationDTO> liquidations);
}