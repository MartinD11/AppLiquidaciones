package com.liquidacionremates.app.mapper;

import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Product;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toProductDTO(Product product);
    Product toEntity(ProductDTO productDTO);
}
