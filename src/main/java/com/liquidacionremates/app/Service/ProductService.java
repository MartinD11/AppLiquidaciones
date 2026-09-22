package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    ProductDTO findById(Long id);
    Optional<ProductDTO> findByName(String name);
    List<ProductDTO> findAll();
    ProductDTO save(ProductDTO clientDTO);
    ProductDTO update(Long id, ProductDTO productDTO);
    void delete(Long id);
    List<ProductDTO> findAllByClientId(Long id);
    void markAsSold(Long id);
    void markAsUnsold(Long id);
    List<ProductDTO> findAvailableProducts();
    void updateSaleData(Long productId, BigDecimal finalPrice, String status,Long buyerId);
    Page<ProductDTO> getProductsByAuctionPaged(Long auctionId, Pageable pageable);
    List<ProductDTO> searchByNameList(String query);
    List<ProductDTO> findByLotNumber(Integer lotNumber);
    void importProductsFromExcel(MultipartFile file);
}
