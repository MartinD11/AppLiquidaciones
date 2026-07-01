package com.liquidacionremates.app.dto;

import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Liquidation;
import com.liquidacionremates.app.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProductDTO {
    private Long id;
    private String name;
    private Integer lotNumber;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private ProductStatus status;
    private Client seller = new Client();
    private Auction auction;
    private Liquidation liquidation;
    private Client buyer;

}
