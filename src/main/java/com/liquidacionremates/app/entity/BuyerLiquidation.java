package com.liquidacionremates.app.entity;

import com.liquidacionremates.app.enums.LiquidationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "buyer_liquidations")
public class BuyerLiquidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Client buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @OneToMany(mappedBy = "buyerLiquidation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Product> purchasedProducts = new ArrayList<>();

    private BigDecimal totalBought;
    private BigDecimal addedCommission;
    private BigDecimal netToCollect;

    @Enumerated(EnumType.STRING)
    private LiquidationStatus status;
}