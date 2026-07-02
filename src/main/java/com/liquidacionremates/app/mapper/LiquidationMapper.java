package com.liquidacionremates.app.mapper;
import com.liquidacionremates.app.dto.LiquidationDTO;
import com.liquidacionremates.app.entity.Liquidation;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LiquidationMapper {
    LiquidationDTO toLiquidationDTO(Liquidation liquidation);
    Liquidation toEntity(LiquidationDTO liquidationDTO);

    List<LiquidationDTO> toLiquidationDTO(List<Liquidation> liquidations);
    List<Liquidation> toEntity(List<LiquidationDTO> liquidationDTOs);
}
