package com.liquidacionremates.app.dto;

import com.liquidacionremates.app.enums.LiquidationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BuyerLiquidationDTO {
    private Long id;
    private ClientDTO buyer;
    private AuctionDTO auction;
    private List<ProductDTO> purchasedProducts;
    private BigDecimal totalBought;
    private BigDecimal addedCommission;
    private BigDecimal netToCollect;
    private LiquidationStatus status;
}