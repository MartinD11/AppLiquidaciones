package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.ClientRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.InvalidExcelException;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.ProductMapper;
import com.liquidacionremates.app.utils.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ClientRepository clientRepository;
    private final AuctionRepository auctionRepository;
    private final ExcelHelper excelHelper;

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
        return productRepository.findByActiveTrue().stream()
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setActive(false);
        productRepository.save(product);
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

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> findAvailableProducts() {
        return productRepository.findByStatusAndAuctionIsNullAndActiveTrue(ProductStatus.NOT_SOLD)
                .stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void updateSaleData(Long productId, BigDecimal salePrice, String status, Long buyerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        product.setSalePrice(salePrice);
        product.setStatus(ProductStatus.valueOf(status));

        if (buyerId != null) {
            Client buyer = clientRepository.findById(buyerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            product.setBuyer(buyer);
        } else {
            product.setBuyer(null);
        }
    }

    @Override
    public Page<ProductDTO> getProductsByAuctionPaged(Long auctionId, Pageable pageable) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el remate"));

        return productRepository.findAllByAuction_Id(auctionId,pageable)
                .map(productMapper::toProductDTO);

    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> searchByNameList(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query).stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> findByLotNumber(Integer lotNumber) {
        if (lotNumber == null) {
            return List.of();
        }
        return productRepository.findAllByLotNumber(lotNumber).stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void importProductsFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelException("El archivo Excel está vacío o no fue seleccionado.");
        }

        try {
            List<ProductDTO> productDTOs = excelHelper.parseExcelFile(file.getInputStream());

            for (ProductDTO dto : productDTOs) {

                boolean isNameEmpty = dto.getName() == null || dto.getName().trim().isEmpty();
                boolean isSellerEmpty = dto.getSeller() == null || dto.getSeller().getName() == null || dto.getSeller().getName().trim().isEmpty();

                if (isNameEmpty && isSellerEmpty) {
                    continue;
                }

                Product product = new Product();
                product.setName(isNameEmpty ? "Producto sin nombre" : dto.getName().trim());
                product.setLotNumber(dto.getLotNumber());
                product.setBasePrice(dto.getBasePrice());
                product.setStatus(ProductStatus.NOT_SOLD);
                product.setActive(true);

                String fullExcelName = isSellerEmpty ? "A CONFIRMAR" : dto.getSeller().getName().trim();

                Client client = clientRepository.findByFullName(fullExcelName)
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            Client newClient = new Client();
                            String[] nameParts = fullExcelName.split(" ", 2);
                            newClient.setName(nameParts[0]);
                            newClient.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                            return clientRepository.save(newClient);
                        });

                product.setSeller(client);
                productRepository.save(product);
            }

        } catch (IOException e) {
            throw new InvalidExcelException("Ocurrió un problema al leer el archivo: " + e.getMessage());
        }
    }

}
