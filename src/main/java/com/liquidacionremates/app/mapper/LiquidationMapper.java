package com.liquidacionremates.app.mapper;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.entity.Liquidation;

import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface LiquidationMapper {
    LiquidationDTO toLiquidationDTO(Liquidation liquidation);
    Liquidation toEntity(LiquidationDTO liquidationDTO);
}
