package com.liquidacionremates.app.mapper;

import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientDTO toClientDTO(Client client);
    Client toEntity(ClientDTO clientDTO);
}
