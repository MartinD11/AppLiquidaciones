package com.liquidacionremates.app.Repository;

import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.ProductStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    Optional<Product> findByName(String name);

    List<Product> findAllBySeller_Id(Long id);

    @Modifying
    @Transactional
    @Query("Update Product p SET p.status = 'SOLD' WHERE p.id=:id")
    void markAsSold(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status = 'NO_VENDIDO' WHERE p.id = :id")
    void markAsUnsold(Long id);

    List<Product> findByStatusAndAuctionIsNull(ProductStatus status);

    List<Product> findByAuctionIdAndStatus(Long auctionId, ProductStatus productStatus);

    Page<Product> findAllByAuction_Id(Long auctionId, Pageable pageable);
}
