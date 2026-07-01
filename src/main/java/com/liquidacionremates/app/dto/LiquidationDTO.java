package com.liquidacionremates.app.dto;

import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.LiquidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiquidationDTO {
    private Long id;
    private LocalDate generationDate;
    private BigDecimal totalSold;
    private BigDecimal retainedCommission;
    private BigDecimal netToPay;
    private LiquidationStatus status;
    private Client client;
    private Auction auction;
    private List<Product> liquidatedProducts;
}
