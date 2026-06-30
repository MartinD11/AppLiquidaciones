package com.liquidacionremates.app.entity;

import com.liquidacionremates.app.enums.LiquidationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "liquidations")
@Getter @Setter @NoArgsConstructor
public class Liquidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate generationDate;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalSold;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal retainedCommission;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal netToPay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LiquidationStatus status = LiquidationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    // mappedBy = "liquidation" porque así se llama la variable en Product
    @OneToMany(mappedBy = "liquidation")
    private List<Product> liquidatedProducts = new ArrayList<>();

    public String getVisualIdentifier() {
        if (this.generationDate == null) return "Liquidation without date";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Corregí "Remate del" por "Liquidation of"
        return "Liquidation of " + this.generationDate.format(formatter);
    }
}