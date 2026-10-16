package com.liquidacionremates.app.Repository;

import com.liquidacionremates.app.entity.BuyerLiquidation;
import com.liquidacionremates.app.enums.LiquidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerLiquidationRepository extends JpaRepository<BuyerLiquidation, Long> {

    List<BuyerLiquidation> findByAuctionId(Long auctionId);

    List<BuyerLiquidation> findByAuctionIdAndBuyerId(Long auctionId, Long buyerId);

    Optional<BuyerLiquidation> findByAuctionIdAndBuyerIdAndStatus(Long auctionId, Long buyerId, LiquidationStatus status);
}