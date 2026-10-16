package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.BuyerLiquidationRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.BuyerLiquidationDTO;
import com.liquidacionremates.app.dto.BuyerLiquidationSummaryDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.BuyerLiquidation;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.LiquidationStatus;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.BuyerLiquidationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerLiquidationServiceImpl implements BuyerLiquidationService {

    private final BuyerLiquidationRepository buyerRepo;
    private final AuctionRepository auctionRepo;
    private final ProductRepository productRepo;
    private final BuyerLiquidationMapper buyerMapper; // Inyectamos el mapper para los DTOs

    @Transactional
    @Override
    public void generateOrUpdateForAuction(Long auctionId) {
        Auction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + auctionId));

        List<Product> soldProducts = auction.getSoldProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.SOLD && p.getBuyer() != null)
                .collect(Collectors.toList());

        Map<Client, List<Product>> productsByBuyer = soldProducts.stream()
                .collect(Collectors.groupingBy(Product::getBuyer));

        for (Map.Entry<Client, List<Product>> entry : productsByBuyer.entrySet()) {
            Client buyer = entry.getKey();
            List<Product> purchased = entry.getValue();

            BuyerLiquidation liqui = buyerRepo.findByAuctionIdAndBuyerId(auctionId, buyer.getId())
                    .stream().findFirst()
                    .orElse(new BuyerLiquidation());

            liqui.setAuction(auction);
            liqui.setBuyer(buyer);
            if (liqui.getStatus() == null) {
                liqui.setStatus(LiquidationStatus.PENDING);
            }

            BigDecimal totalBought = purchased.stream()
                    .map(Product::getSalePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal addedCommission = totalBought.multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal netToCollect = totalBought.add(addedCommission);

            liqui.setTotalBought(totalBought);
            liqui.setAddedCommission(addedCommission);
            liqui.setNetToCollect(netToCollect);

            for (Product p : purchased) {
                p.setBuyerLiquidation(liqui);
            }
            liqui.setPurchasedProducts(purchased);

            buyerRepo.save(liqui);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BuyerLiquidationDTO> getLiquidations(Long auctionId, Long buyerId) {
        List<BuyerLiquidation> liquidations;

        if (buyerId != null) {
            liquidations = buyerRepo.findByAuctionIdAndBuyerId(auctionId, buyerId);
        } else {
            liquidations = buyerRepo.findByAuctionId(auctionId);
        }

        return liquidations.stream()
                .map(buyerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void markAsPaid(Long id) {
        BuyerLiquidation liqui = buyerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cobranza no encontrada con ID: " + id));

        liqui.setStatus(LiquidationStatus.PAID);
        buyerRepo.save(liqui);
    }

    @Override
    public BuyerLiquidationSummaryDTO getSummary(List<BuyerLiquidationDTO> liquidations) {
        if (liquidations == null || liquidations.isEmpty()) {
            return new BuyerLiquidationSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal totalBought = liquidations.stream()
                .map(BuyerLiquidationDTO::getTotalBought)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal addedCommission = liquidations.stream()
                .map(BuyerLiquidationDTO::getAddedCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netToCollect = liquidations.stream()
                .map(BuyerLiquidationDTO::getNetToCollect)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BuyerLiquidationSummaryDTO(totalBought, addedCommission, netToCollect);
    }
}