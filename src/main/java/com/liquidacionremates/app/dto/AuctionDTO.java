package com.liquidacionremates.app.dto;

import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDTO {
    private Long id;
    private LocalDate date;
    private List<Product>soldProducts;

}
