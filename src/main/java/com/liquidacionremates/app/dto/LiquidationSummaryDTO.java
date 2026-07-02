package com.liquidacionremates.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LiquidationSummaryDTO {
    private BigDecimal totalSold;
    private BigDecimal totalCommission;
    private BigDecimal totalNetToPay;
}
