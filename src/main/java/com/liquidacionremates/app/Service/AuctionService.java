package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.dto.AuctionDTO;

import java.time.LocalDate;
import java.util.List;

public interface AuctionService {
    AuctionDTO save(AuctionDTO auctionDTO);
    AuctionDTO update(Long id,AuctionDTO auctionDTO);
    void delete(Long id);
    AuctionDTO findByDate(LocalDate date);
    AuctionDTO findById(Long id);
    List<AuctionDTO> findAll();
    void addProductsToAuction(Long auctionId,List<Long>productIds);
    void removeProductFromAuction(Long productId);
}
