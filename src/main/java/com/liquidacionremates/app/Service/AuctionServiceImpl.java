package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.AuctionDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.AuctionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return auctionMapper.toAuctionDTO(auctionRepository.save(auction));
    }

    @Transactional
    @Override
    public AuctionDTO update(Long id, AuctionDTO auctionDTO) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        if(auctionDTO.getDate()!=null){
            auction.setDate(auctionDTO.getDate());
        }

        return auctionMapper.toAuctionDTO(auctionRepository.save(auction));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        List<Product> products = auction.getSoldProducts();
        for(Product product:products){
            product.setAuction(null);
        }

        auctionRepository.delete(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public AuctionDTO findByDate(LocalDate date) {
        Auction auction = auctionRepository.findByDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró remate para la fecha: " + date));

        return auctionMapper.toAuctionDTO(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public AuctionDTO findById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remate no encontrado con ID: " + id));

        return auctionMapper.toAuctionDTO(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuctionDTO> findAll() {
        return auctionRepository.findAll().stream()
                .map(auctionMapper::toAuctionDTO)
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
    }
}
