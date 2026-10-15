package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.AuctionRepository;
import com.liquidacionremates.app.Repository.LiquidationRepository;
import com.liquidacionremates.app.Repository.ProductRepository;
import com.liquidacionremates.app.dto.LiquidationSummaryDTO;
import com.liquidacionremates.app.entity.Auction;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.entity.Liquidation;
import com.liquidacionremates.app.entity.Product;
import com.liquidacionremates.app.enums.LiquidationStatus;
import com.liquidacionremates.app.enums.ProductStatus;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.AuctionMapper;
import com.liquidacionremates.app.mapper.LiquidationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de Lógica Financiera y Liquidaciones - RematePro")
class LiquidationServiceImplTest {

    @Mock
    private LiquidationRepository liquidationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private LiquidationMapper liquidationMapper;

    @Mock
    private AuctionMapper auctionMapper;

    @InjectMocks
    private LiquidationServiceImpl liquidationService;

    @Captor
    private ArgumentCaptor<Liquidation> liquidationCaptor;

    @Captor
    private ArgumentCaptor<List<Product>> productsListCaptor;

    private Auction defaultAuction;
    private Client defaultSeller;

    @BeforeEach
    void setUp() {
        defaultAuction = new Auction();
        defaultAuction.setId(1L);
        defaultAuction.setDate(LocalDate.of(2026, 9, 21));
        defaultAuction.setActive(true);

        defaultSeller = new Client();
        defaultSeller.setId(10L);
        defaultSeller.setName("Carlos");
        defaultSeller.setLastName("Gómez");
        defaultSeller.setActive(true);
    }

    // =========================================================================
    // 1. CÁLCULO DE COMISIONES Y RETENCIONES (PRECISIÓN MONETARIA)
    // =========================================================================

    @Nested
    @DisplayName("Pruebas de Cálculo de Comisiones y Retenciones")
    class CommissionCalculationTests {

        @Test
        @DisplayName("Debe calcular exactamente comisiones y neto a pagar con múltiples lotes vendidos")
        void generateLiquidations_whenMultipleLotsSold_shouldCalculateExactCommissionsAndNet() {
            Long auctionId = 1L;

            Product lot1 = createProduct(101L, "Tractor Fiat 700", 1, new BigDecimal("10000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);
            Product lot2 = createProduct(102L, "Arado de Disco", 2, new BigDecimal("5000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);
            Product lot3 = createProduct(103L, "Sembradora Antigua", 3, new BigDecimal("2500.50"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            List<Product> soldProducts = Arrays.asList(lot1, lot2, lot3);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD)).thenReturn(soldProducts);
            // Simulamos que la liquidación aún no existe para este cliente (Optional.empty)
            when(liquidationRepository.findByAuctionIdAndClientId(auctionId, defaultSeller.getId())).thenReturn(Optional.empty());
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BigDecimal expectedTotalSold = new BigDecimal("17500.50").setScale(2, RoundingMode.HALF_UP);
            BigDecimal expectedCommission = expectedTotalSold.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP); // 1750.05
            BigDecimal expectedNetToPay = expectedTotalSold.subtract(expectedCommission).setScale(2, RoundingMode.HALF_UP);      // 15750.45
            BigDecimal expectedCommissionRate = new BigDecimal("10.00");

            liquidationService.generateLiquidationsForAuction(auctionId);

            verify(liquidationRepository, times(1)).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            assertNotNull(savedLiquidation);
            assertEquals(defaultAuction, savedLiquidation.getAuction());
            assertEquals(defaultSeller, savedLiquidation.getClient());
            assertEquals(LiquidationStatus.PENDING, savedLiquidation.getStatus());

            assertEquals(0, savedLiquidation.getTotalSold().compareTo(expectedTotalSold));
            assertEquals(0, savedLiquidation.getRetainedCommission().compareTo(expectedCommission));
            assertEquals(0, savedLiquidation.getNetToPay().compareTo(expectedNetToPay));
            assertEquals(0, savedLiquidation.getCommissionPercentage().compareTo(expectedCommissionRate));

            verify(productRepository, times(1)).saveAll(productsListCaptor.capture());
            List<Product> updatedProducts = productsListCaptor.getValue();
            assertEquals(3, updatedProducts.size());
        }

        @Test
        @DisplayName("Debe generar liquidaciones independientes y cálculos separados por cada vendedor")
        void generateLiquidations_whenMultipleSellersInSameAuction_shouldSegregateLiquidations() {
            Long auctionId = 1L;

            Client sellerB = new Client();
            sellerB.setId(20L);
            sellerB.setName("María");
            sellerB.setLastName("López");
            sellerB.setActive(true);

            Product p1 = createProduct(1L, "Lote A1", 1, new BigDecimal("4000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);
            Product p2 = createProduct(2L, "Lote B1", 2, new BigDecimal("6000.00"), ProductStatus.SOLD, sellerB, defaultAuction);
            Product p3 = createProduct(3L, "Lote B2", 3, new BigDecimal("2000.00"), ProductStatus.SOLD, sellerB, defaultAuction);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Arrays.asList(p1, p2, p3));
            when(liquidationRepository.findByAuctionIdAndClientId(auctionId, defaultSeller.getId())).thenReturn(Optional.empty());
            when(liquidationRepository.findByAuctionIdAndClientId(auctionId, sellerB.getId())).thenReturn(Optional.empty());
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            liquidationService.generateLiquidationsForAuction(auctionId);

            verify(liquidationRepository, times(2)).save(liquidationCaptor.capture());
            List<Liquidation> capturedLiquidations = liquidationCaptor.getAllValues();

            assertEquals(2, capturedLiquidations.size());

            Liquidation liqSellerA = capturedLiquidations.stream()
                    .filter(l -> l.getClient().getId().equals(defaultSeller.getId()))
                    .findFirst().get();

            Liquidation liqSellerB = capturedLiquidations.stream()
                    .filter(l -> l.getClient().getId().equals(sellerB.getId()))
                    .findFirst().get();

            assertEquals(0, liqSellerA.getTotalSold().compareTo(new BigDecimal("4000.00")));
            assertEquals(0, liqSellerB.getTotalSold().compareTo(new BigDecimal("8000.00")));
        }
    }

    // =========================================================================
    // 2. RECÁLCULO Y CASOS BORDE
    // =========================================================================

    @Nested
    @DisplayName("Pruebas de Recálculo y Ausencia de Ventas")
    class EdgeCasesAndRecalculationTests {

        @Test
        @DisplayName("Debe procesar correctamente liquidación cuando los lotes se vendieron a $0.00")
        void generateLiquidations_whenProductsSoldAtZero_shouldGenerateZeroAmountsLiquidation() {
            Long auctionId = 1L;

            Product zeroLot1 = createProduct(201L, "Lote Donación 1", 1, BigDecimal.ZERO, ProductStatus.SOLD, defaultSeller, defaultAuction);
            Product zeroLot2 = createProduct(202L, "Lote Donación 2", 2, new BigDecimal("0.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Arrays.asList(zeroLot1, zeroLot2));
            when(liquidationRepository.findByAuctionIdAndClientId(auctionId, defaultSeller.getId())).thenReturn(Optional.empty());
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            liquidationService.generateLiquidationsForAuction(auctionId);

            verify(liquidationRepository, times(1)).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            assertEquals(0, savedLiquidation.getTotalSold().compareTo(BigDecimal.ZERO));
            assertEquals(0, savedLiquidation.getRetainedCommission().compareTo(BigDecimal.ZERO));
            assertEquals(0, savedLiquidation.getNetToPay().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hubo ventas en el remate")
        void generateLiquidations_whenNoProductsSold_shouldThrowExceptionAndNotPersist() {
            Long auctionId = 1L;

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Collections.emptyList());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    liquidationService.generateLiquidationsForAuction(auctionId)
            );

            assertEquals("No hay productos vendidos en este remate para liquidar.", exception.getMessage());
            verify(liquidationRepository, never()).save(any(Liquidation.class));
        }

        @Test
        @DisplayName("Debe actualizar la liquidación existente en lugar de fallar o duplicar (Recálculo)")
        void generateLiquidations_whenAlreadyGenerated_shouldUpdateExisting() {
            Long auctionId = 1L;
            Product lot1 = createProduct(101L, "Lote 1", 1, new BigDecimal("10000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            Liquidation existingLiquidation = new Liquidation();
            existingLiquidation.setId(99L); // Simulamos que ya existe en la BD
            existingLiquidation.setStatus(LiquidationStatus.PENDING);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD)).thenReturn(List.of(lot1));
            // Simulamos que encuentra la liquidación previa
            when(liquidationRepository.findByAuctionIdAndClientId(auctionId, defaultSeller.getId())).thenReturn(Optional.of(existingLiquidation));
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            liquidationService.generateLiquidationsForAuction(auctionId);

            verify(liquidationRepository).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            // Verificamos que guardó la entidad existente (ID 99L) y no una nueva
            assertEquals(99L, savedLiquidation.getId(), "Debe actualizar la liquidación existente");
            assertEquals(0, savedLiquidation.getTotalSold().compareTo(new BigDecimal("10000.00")));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el remate especificado no existe")
        void generateLiquidations_whenAuctionNotFound_shouldThrowException() {
            Long nonExistentAuctionId = 999L;
            when(auctionRepository.findById(nonExistentAuctionId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    liquidationService.generateLiquidationsForAuction(nonExistentAuctionId)
            );

            assertEquals("Remate no encontrado con ID: " + nonExistentAuctionId, exception.getMessage());
        }
    }

    // =========================================================================
    // 3. FLUJO DE PAGO Y RESUMEN
    // =========================================================================

    @Nested
    @DisplayName("Pruebas del Flujo de Pago")
    class PaymentFlowTests {

        @Test
        @DisplayName("Debe realizar la transición segura de estado PENDING a PAID")
        void markAsPaid_whenLiquidationIsPending_shouldTransitionToPaidSuccessfully() {
            Long liquidationId = 50L;
            Liquidation pendingLiquidation = new Liquidation();
            pendingLiquidation.setId(liquidationId);
            pendingLiquidation.setStatus(LiquidationStatus.PENDING);

            when(liquidationRepository.findById(liquidationId)).thenReturn(Optional.of(pendingLiquidation));
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            liquidationService.markAsPaid(liquidationId);

            verify(liquidationRepository).save(liquidationCaptor.capture());
            assertEquals(LiquidationStatus.PAID, liquidationCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("Debe rechazar el doble pago lanzando excepción si ya estaba PAID")
        void markAsPaid_whenLiquidationAlreadyPaid_shouldThrowException() {
            Long liquidationId = 50L;
            Liquidation alreadyPaidLiquidation = new Liquidation();
            alreadyPaidLiquidation.setId(liquidationId);
            alreadyPaidLiquidation.setStatus(LiquidationStatus.PAID);

            when(liquidationRepository.findById(liquidationId)).thenReturn(Optional.of(alreadyPaidLiquidation));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    liquidationService.markAsPaid(liquidationId)
            );

            assertEquals("Esta liquidación ya fue marcada como pagada.", exception.getMessage());
            verify(liquidationRepository, never()).save(any(Liquidation.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción al intentar pagar una liquidación inexistente")
        void markAsPaid_whenLiquidationNotFound_shouldThrowException() {
            Long nonExistentId = 9999L;
            when(liquidationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    liquidationService.markAsPaid(nonExistentId)
            );

            assertEquals("Liquidación no encontrada con ID: " + nonExistentId, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Pruebas de Resumen y Agregación Financiera")
    class SummaryAggregationTests {

        @Test
        @DisplayName("Debe acumular totales con precisión")
        void getSummaryByAuction_shouldAggregateTotalsAndCommissionsAccurately() {
            Long auctionId = 1L;

            Liquidation l1 = new Liquidation();
            l1.setTotalSold(new BigDecimal("12500.50"));
            l1.setRetainedCommission(new BigDecimal("1250.05"));
            l1.setNetToPay(new BigDecimal("11250.45"));

            Liquidation l2 = new Liquidation();
            l2.setTotalSold(new BigDecimal("8000.00"));
            l2.setRetainedCommission(new BigDecimal("800.00"));
            l2.setNetToPay(new BigDecimal("7200.00"));

            when(liquidationRepository.findByAuctionId(auctionId)).thenReturn(Arrays.asList(l1, l2));

            LiquidationSummaryDTO summary = liquidationService.getSummaryByAuction(auctionId);

            assertEquals(0, summary.getTotalSold().compareTo(new BigDecimal("20500.50")));
            assertEquals(0, summary.getTotalCommission().compareTo(new BigDecimal("2050.05")));
            assertEquals(0, summary.getTotalNetToPay().compareTo(new BigDecimal("18450.45")));
        }
    }

    private Product createProduct(Long id, String name, Integer lotNumber, BigDecimal salePrice,
                                  ProductStatus status, Client seller, Auction auction) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setLotNumber(lotNumber);
        product.setSalePrice(salePrice);
        product.setStatus(status);
        product.setActive(true);
        product.setSeller(seller);
        product.setAuction(auction);
        return product;
    }
}