package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.LiquidationRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.dto.LiquidationSummaryDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Liquidation;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.LiquidationStatus;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.AuctionMapper;
import com.liquidacionremates.app.mapper.LiquidationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiquidationServiceImpl implements LiquidationService {

    private final LiquidationRepository liquidationRepository;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final LiquidationMapper liquidationMapper;
    private final AuctionMapper auctionMapper;

    @Transactional
    @Override
    public void generateLiquidationsForAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + auctionId));

        List<Product> soldProducts = productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD);

        if(soldProducts.isEmpty()) {
            throw new ResourceNotFoundException("No hay productos vendidos en este remate para liquidar.");
        }

        Map<Client,List<Product>> productsBySeller = soldProducts.stream()
                .collect(Collectors.groupingBy(Product::getSeller));

        BigDecimal commissionRate = new BigDecimal("10.00");
        BigDecimal commissionMultiplier = new BigDecimal("0.10");

        for(Map.Entry<Client,List<Product>> entry : productsBySeller.entrySet()) {
            Client seller = entry.getKey();
            List<Product> clientsProducts = entry.getValue();

            BigDecimal totalSold = clientsProducts.stream()
                    .map(Product::getSalePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal retainedCommission = totalSold.multiply(commissionMultiplier).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netToPay = totalSold.subtract(retainedCommission).setScale(2, RoundingMode.HALF_UP);

            // Buscamos si ya existe. Si no, instanciamos una nueva.
            Liquidation liquidation = liquidationRepository.findByAuctionIdAndClientId(auctionId, seller.getId())
                    .orElse(new Liquidation());

            // Si es nueva, completamos los campos fijos
            if (liquidation.getId() == null) {
                liquidation.setAuction(auction);
                liquidation.setClient(seller);
                liquidation.setGenerationDate(LocalDate.now());
                liquidation.setCommissionPercentage(commissionRate);
                liquidation.setStatus(LiquidationStatus.PENDING);
            }

            // Actualizamos montos y recalculamos (para nuevas y existentes)
            liquidation.setTotalSold(totalSold);
            liquidation.setRetainedCommission(retainedCommission);
            liquidation.setNetToPay(netToPay);

            Liquidation savedLiquidation = liquidationRepository.save(liquidation);

            // Reasignamos los productos (ideal para atrapar los rezagados)
            for(Product product : clientsProducts) {
                product.setLiquidation(savedLiquidation);
            }

            productRepository.saveAll(clientsProducts);
        }
    }

    @Override
    public List<LiquidationDTO> getLiquidationsByAuction(Long auctionId) {
        List<Liquidation> liquidations = liquidationRepository.findByAuctionId(auctionId);
        return liquidations.stream()
                .map(liquidationMapper::toLiquidationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void markAsPaid(Long liquidationId) {
        Liquidation liquidation = liquidationRepository.findById(liquidationId)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con ID: " + liquidationId));

        if (liquidation.getStatus() == LiquidationStatus.PAID) {
            throw new IllegalStateException("Esta liquidación ya fue marcada como pagada.");
        }

        liquidation.setStatus(LiquidationStatus.PAID);
        liquidationRepository.save(liquidation);
    }

    @Override
    public LiquidationDTO findById(Long id) {
        Liquidation liquidation = liquidationRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Liquidacion no encontrada con el Id: " + id));
        return liquidationMapper.toLiquidationDTO(liquidation);
    }

    @Override
    public LiquidationSummaryDTO getSummaryByAuction(Long auctionId) {
        List<Liquidation> liquidations = liquidationRepository.findByAuctionId(auctionId);
        BigDecimal totalSold = liquidations.stream().map(Liquidation::getTotalSold).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = liquidations.stream().map(Liquidation::getRetainedCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = liquidations.stream().map(Liquidation::getNetToPay).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LiquidationSummaryDTO(totalSold, totalCommission, totalNet);
    }

    @Override
    public List<LiquidationDTO> getFilteredLiquidations(Long auctionId, Long clientId) {
        Long idToSearch = (clientId != null && clientId <= 0) ? null : clientId;
        List<Liquidation> entities = liquidationRepository.findByAuctionIdAndOptionalClient(auctionId, idToSearch);
        return liquidationMapper.toLiquidationDTO(entities);
    }

    @Override
    public LiquidationSummaryDTO getSummaryByAuctionAndClient(Long auctionId, Long clientId) {
        Long idToSearch = (clientId != null && clientId <= 0) ? null : clientId;
        List<Liquidation> list = liquidationRepository.findByAuctionIdAndOptionalClient(auctionId, idToSearch);
        BigDecimal totalSold = list.stream().map(Liquidation::getTotalSold).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = list.stream().map(Liquidation::getRetainedCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = list.stream().map(Liquidation::getNetToPay).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LiquidationSummaryDTO(totalSold, totalCommission, totalNet);
    }

    @Override
    public boolean hasLiquidationsForAuction(Long auctionId) {
        return !liquidationRepository.findByAuctionId(auctionId).isEmpty();
    }
}