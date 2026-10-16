package com.liquidacionremates.app.mapper;

import com.liquidacionremates.app.dto.BuyerLiquidationDTO;
import com.liquidacionremates.app.entity.BuyerLiquidation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BuyerLiquidationMapper {

    BuyerLiquidationDTO toDTO(BuyerLiquidation entity);

    BuyerLiquidation toEntity(BuyerLiquidationDTO dto);
}