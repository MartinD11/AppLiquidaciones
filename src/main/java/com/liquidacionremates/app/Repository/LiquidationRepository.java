package com.liquidacionremates.app.Repository;

import com.liquidacionremates.app.entity.Liquidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiquidationRepository extends JpaRepository<Liquidation, Long> {

    List<Liquidation> findByAuctionId(Long auctionId);

    boolean existsByAuctionId(Long auctionId);

    List<Liquidation> findByClientId(Long clientId);

    @Query("SELECT l FROM Liquidation l WHERE l.auction.id = :auctionId AND (:clientId IS NULL OR l.client.id = :clientId)")
    List<Liquidation> findByAuctionIdAndOptionalClient(@Param("auctionId") Long auctionId, @Param("clientId") Long clientId);
}
