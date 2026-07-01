package com.liquidacionremates.app.mapper;

import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.entity.Auction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuctionMapper {
    AuctionDTO toAuctionDTO(Auction auction);
    Auction toEntity(AuctionDTO auctionDTO);
}
