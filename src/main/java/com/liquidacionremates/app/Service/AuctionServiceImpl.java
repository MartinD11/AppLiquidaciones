package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.AuctionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AuctionMapper auctionMapper;

    @Transactional
    @Override
    public AuctionDTO save(AuctionDTO auctionDTO) {
        Auction auction = auctionMapper.toEntity(auctionDTO);
        Auction savedAuction = auctionRepository.save(auction);
        return mapToDTOWithTotal(savedAuction);
    }

    @Transactional
    @Override
    public AuctionDTO update(Long id, AuctionDTO auctionDTO) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        if(auctionDTO.getDate()!=null){
            auction.setDate(auctionDTO.getDate());
        }

        Auction updatedAuction = auctionRepository.save(auction);
        return mapToDTOWithTotal(updatedAuction);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        List<Product> products = auction.getSoldProducts();
        for(Product product : products){
            product.setAuction(null);
        }

        auction.setActive(false);
        auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public AuctionDTO findByDate(LocalDate date) {
        Auction auction = auctionRepository.findByDateAndActiveTrue(date)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró remate activo para la fecha: " + date));

        return mapToDTOWithTotal(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public AuctionDTO findById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        return mapToDTOWithTotal(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuctionDTO> findAll() {
        return auctionRepository.findByActiveTrue().stream()
                .map(this::mapToDTOWithTotal)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void addProductsToAuction(Long auctionId, List<Long> productIds) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + auctionId));

        List<Product> products = productRepository.findAllById(productIds);
        for (Product product : products) {
            product.setAuction(auction);
        }
    }

    @Transactional
    @Override
    public void removeProductFromAuction(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        product.setAuction(null);
        product.setBuyer(null);
        product.setSalePrice(null);
        product.setStatus(ProductStatus.NOT_SOLD);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuctionDTO> findAllHistorical() {
        return auctionRepository.findAll().stream()
                .map(this::mapToDTOWithTotal)
                .collect(Collectors.toList());
    }

    private AuctionDTO mapToDTOWithTotal(Auction auction) {
        AuctionDTO dto = auctionMapper.toAuctionDTO(auction);
        dto.setTotalCollected(calculateTotalCollected(auction));
        dto.setSoldLotsCount(calculateSoldLotsCount(auction));

        return dto;
    }

    private BigDecimal calculateTotalCollected(Auction auction) {
        if (auction.getSoldProducts() == null || auction.getSoldProducts().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return auction.getSoldProducts().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == ProductStatus.SOLD && p.getSalePrice() != null)
                .map(Product::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculateSoldLotsCount(Auction auction) {
        if (auction.getSoldProducts() == null || auction.getSoldProducts().isEmpty()) {
            return 0;
        }
        return (int) auction.getSoldProducts().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == ProductStatus.SOLD)
                .count();
    }
}