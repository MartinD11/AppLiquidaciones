package com.liquidacionremates.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerLiquidationSummaryDTO {
    private BigDecimal totalBought;
    private BigDecimal addedCommission;
    private BigDecimal netToCollect;
}