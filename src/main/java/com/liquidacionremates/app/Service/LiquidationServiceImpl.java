package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.LiquidationRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.dto.LiquidationSummaryDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Liquidation;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.LiquidationStatus;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.LiquidationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Transactional
    public void generateLiquidationsForAuction(Long auctionId) {
        if(liquidationRepository.existsById(auctionId)) {
            throw new RuntimeException("Las liquidaciones para este remate ya fueron generadas.");
        }

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Remate no encontrado"));

        List<Product> soldProducts = productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD);

        if(soldProducts.isEmpty()) {
            throw new RuntimeException("No hay productos vendidos en este remate para liquidar.");
        }

        //  Agrupar productos por Dueño (Vendedor) usando Java Streams
        Map<Client,List<Product>> productsBySeller = soldProducts.stream()
                .collect(Collectors.groupingBy(Product::getSeller));

        BigDecimal commissionRate = new BigDecimal("10.00");
        BigDecimal commissionMultiplier = new BigDecimal("0.10");

        for(Map.Entry<Client,List<Product>> entry : productsBySeller.entrySet()) {
            Client seller = entry.getKey();
            List<Product> clientsProducts = entry.getValue();

            //sumo los precios de venta de todos los productos de determinado cliente
            BigDecimal totalSold = clientsProducts.stream()
                    .map(Product::getSalePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // calcular comisiones y neto
            BigDecimal retainedCommission = totalSold.multiply(commissionMultiplier);
            BigDecimal netToPay = totalSold.subtract(retainedCommission);

            Liquidation liquidation = new Liquidation();
            liquidation.setAuction(auction);
            liquidation.setClient(seller);
            liquidation.setGenerationDate(LocalDate.now());
            liquidation.setTotalSold(totalSold);
            liquidation.setRetainedCommission(retainedCommission);
            liquidation.setNetToPay(netToPay);
            liquidation.setCommissionPercentage(commissionRate);
            liquidation.setStatus(LiquidationStatus.PENDING);

            Liquidation savedLiquidation = liquidationRepository.save(liquidation);

            //vinculo los pructos a cada liquidacion
            for(Product product : clientsProducts) {
                product.setLiquidation(savedLiquidation);
            }

            productRepository.saveAll(clientsProducts);
        }

    }

    public List<LiquidationDTO> getLiquidationsByAuction(Long auctionId) {
        List<Liquidation> liquidations = liquidationRepository.findByAuctionId(auctionId);

        return liquidations.stream()
                .map(liquidationMapper::toLiquidationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsPaid(Long liquidationId) {
        Liquidation liquidation = liquidationRepository.findById(liquidationId)
                .orElseThrow(() -> new RuntimeException("Liquidación no encontrada"));

        if (liquidation.getStatus() == LiquidationStatus.PAID) {
            throw new RuntimeException("Esta liquidación ya fue marcada como pagada.");
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

        BigDecimal totalSold = liquidations.stream()
                .map(Liquidation::getTotalSold)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = liquidations.stream()
                .map(Liquidation::getRetainedCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = liquidations.stream()
                .map(Liquidation::getNetToPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new LiquidationSummaryDTO(totalSold, totalCommission, totalNet);
    }


    @Override
    public List<LiquidationDTO> getFilteredLiquidations(Long auctionId, Long clientId) {
        // Si clientId es 0, lo convertimos a null para que la query lo ignore
        Long idToSearch = (clientId != null && clientId <= 0) ? null : clientId;

        List<Liquidation> entities = liquidationRepository.findByAuctionIdAndOptionalClient(auctionId, idToSearch);

        return liquidationMapper.toLiquidationDTO(entities);
    }

    @Override
    public LiquidationSummaryDTO getSummaryByAuctionAndClient(Long auctionId, Long clientId) {
        // 1. Aplicamos la misma lógica de limpieza de ID que en getFilteredLiquidations
        Long idToSearch = (clientId != null && clientId <= 0) ? null : clientId;

        List<Liquidation> list = liquidationRepository.findByAuctionIdAndOptionalClient(auctionId, idToSearch);

        // 3. Calculamos totales (esto no cambia, está perfecto así)
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
