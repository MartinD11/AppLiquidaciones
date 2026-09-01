package com.liquidacionremates.app.Service;


import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.dto.LiquidationSummaryDTO;
import com.liquidacionremates.app.entity.Liquidation;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface LiquidationService {
    void generateLiquidationsForAuction(Long auctionId);
    List<LiquidationDTO> getLiquidationsByAuction(Long auctionId);
    void markAsPaid(Long liquidationId);
    LiquidationSummaryDTO getSummaryByAuction(Long auctionId);
    LiquidationDTO findById(Long id);
    List<LiquidationDTO> getFilteredLiquidations(Long auctionId, Long clientId);
    boolean hasLiquidationsForAuction(Long auctionId);
    @Nullable Object getSummaryByAuctionAndClient(Long auctionId, Long clientId);
}
