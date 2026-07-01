package com.liquidacionremates.app.Repository;

import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction,Long> {

    Optional<Auction> findByDate(LocalDate date);
}
