package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.ClientRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    @Override
    public ProductDTO findById(Long id) {
        Product product  = productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product not found with ID: " + id));

        return productMapper.toProductDTO(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductDTO> findByName(String name) {
        return productRepository.findByName(name)
                .map(productMapper::toProductDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ProductDTO save(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);

        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.NOT_SOLD);
        }

        if (productDTO.getSeller() != null && productDTO.getSeller().getId() != null) {
            Client realClient = clientRepository.findById(productDTO.getSeller().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            product.setSeller(realClient);
        }

        return productMapper.toProductDTO(productRepository.save(product));
    }

    @Transactional
    @Override
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product not found with ID: " + id));

        // Actualizamos los campos básicos
        if(productDTO.getName() != null) product.setName(productDTO.getName());
        if(productDTO.getLotNumber() != null) product.setLotNumber(productDTO.getLotNumber());
        if(productDTO.getBasePrice() != null) product.setBasePrice(productDTO.getBasePrice());
        if(productDTO.getSalePrice() != null) product.setSalePrice(productDTO.getSalePrice());
        if(productDTO.getStatus() != null) product.setStatus(productDTO.getStatus());

        if(productDTO.getSeller() != null && productDTO.getSeller().getId() != null) {
            product.setSeller(productDTO.getSeller());
        }

        if(productDTO.getAuction() != null && productDTO.getAuction().getId() != null) {
            product.setAuction(productDTO.getAuction());
        }

        if(productDTO.getLiquidation() != null && productDTO.getLiquidation().getId() != null) {
            product.setLiquidation(productDTO.getLiquidation());
        }

        return productMapper.toProductDTO(productRepository.save(product));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if(!productRepository.existsById(id)) throw new ResourceNotFoundException("Product not found with ID: " + id);
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> findAllByClientId(Long id) {
        return productRepository.findAllBySeller_Id(id).stream()
                .map(product->productMapper.toProductDTO(product))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void markAsSold(Long id) {
        productRepository.markAsSold(id);
    }

    @Transactional
    @Override
    public void markAsUnsold(Long id) {
        productRepository.markAsUnsold(id);
    }
}
