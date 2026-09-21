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

/**
 * Suite de Pruebas Unitarias de Arquitectura Financiera para {@link LiquidationServiceImpl}.
 * <p>
 * Enfoque: Lógica crítica de negocio, cálculo de comisiones/retenciones con precisión monetaria
 * (BigDecimal y RoundingMode), protección contra doble pago (idempotencia) y transiciones seguras de estado.
 * </p>
 *
 * Principios aplicados:
 * - Sin contexto pesado de Spring (@SpringBootTest excluido; ejecución en milisegundos).
 * - Aislamiento total de capas mediante Mockito y {@link MockitoExtension}.
 * - Patrón Arrange-Act-Assert (Given-When-Then) estricto.
 * - Validación monetaria con {@link BigDecimal#compareTo(BigDecimal)} para evitar fallas por disparidad de escala.
 */
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

        /**
         * Caso Borde Obligatorio: Cálculo de comisiones con múltiples lotes vendidos.
         * <p>
         * Regla de Negocio:
         * - Total Vendido = Suma de los precios de venta de cada lote.
         * - Comisión Retenida = Total Vendido * 10% (0.10).
         * - Neto a Pagar = Total Vendido - Comisión Retenida.
         * - El estado inicial de la liquidación debe ser PENDING.
         * - Cada lote vendido debe quedar vinculado bidireccionalmente a la liquidación persistida.
         * </p>
         */
        @Test
        @DisplayName("Debe calcular exactamente comisiones y neto a pagar con múltiples lotes vendidos")
        void generateLiquidations_whenMultipleLotsSold_shouldCalculateExactCommissionsAndNet() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long auctionId = 1L;

            // Lote 1: $10,000.00
            Product lot1 = createProduct(101L, "Tractor Fiat 700", 1, new BigDecimal("10000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);
            // Lote 2: $5,000.00
            Product lot2 = createProduct(102L, "Arado de Disco", 2, new BigDecimal("5000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);
            // Lote 3: $2,500.50 (incluye centavos para verificar precisión decimal)
            Product lot3 = createProduct(103L, "Sembradora Antigua", 3, new BigDecimal("2500.50"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            List<Product> soldProducts = Arrays.asList(lot1, lot2, lot3);

            // Simulación de los repositorios
            when(liquidationRepository.existsById(auctionId)).thenReturn(false);
            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD)).thenReturn(soldProducts);
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Valores esperados calculados según el estándar financiero (escala 2, HALF_UP)
            BigDecimal expectedTotalSold = new BigDecimal("17500.50").setScale(2, RoundingMode.HALF_UP);
            BigDecimal expectedCommission = expectedTotalSold.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP); // 1750.05
            BigDecimal expectedNetToPay = expectedTotalSold.subtract(expectedCommission).setScale(2, RoundingMode.HALF_UP);      // 15750.45
            BigDecimal expectedCommissionRate = new BigDecimal("10.00");

            // =================================================================
            // ACT (When)
            // =================================================================
            liquidationService.generateLiquidationsForAuction(auctionId);

            // =================================================================
            // ASSERT (Then)
            // =================================================================
            verify(liquidationRepository, times(1)).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            assertNotNull(savedLiquidation, "La liquidación generada no debe ser nula");
            assertEquals(defaultAuction, savedLiquidation.getAuction(), "El remate asociado debe ser el provisto");
            assertEquals(defaultSeller, savedLiquidation.getClient(), "El cliente vendedor debe coincidir");
            assertEquals(LiquidationStatus.PENDING, savedLiquidation.getStatus(), "La liquidación debe nacer en estado PENDING");

            // Validaciones matemáticas obligatorias con compareTo()
            assertEquals(0, savedLiquidation.getTotalSold().compareTo(expectedTotalSold),
                    String.format("El total vendido esperado era %s pero se obtuvo %s", expectedTotalSold, savedLiquidation.getTotalSold()));

            assertEquals(0, savedLiquidation.getRetainedCommission().compareTo(expectedCommission),
                    String.format("La comisión retenida esperada era %s pero se obtuvo %s", expectedCommission, savedLiquidation.getRetainedCommission()));

            assertEquals(0, savedLiquidation.getNetToPay().compareTo(expectedNetToPay),
                    String.format("El neto a pagar esperado era %s pero se obtuvo %s", expectedNetToPay, savedLiquidation.getNetToPay()));

            assertEquals(0, savedLiquidation.getCommissionPercentage().compareTo(expectedCommissionRate),
                    "El porcentaje de comisión debe ser exactamente 10.00%");

            // Consistencia contable estricta: TotalVendido - Retención == NetoAPagar
            BigDecimal calculatedDifference = savedLiquidation.getTotalSold().subtract(savedLiquidation.getRetainedCommission());
            assertEquals(0, calculatedDifference.compareTo(savedLiquidation.getNetToPay()),
                    "Inconsistencia contable: TotalVendido menos Retención debe igualar exactamente a NetoAPagar");

            // Verificar vinculación de cada producto con la liquidación guardada
            verify(productRepository, times(1)).saveAll(productsListCaptor.capture());
            List<Product> updatedProducts = productsListCaptor.getValue();
            assertEquals(3, updatedProducts.size(), "Se deben actualizar los 3 productos liquidados");
            for (Product p : updatedProducts) {
                assertSame(savedLiquidation, p.getLiquidation(), "Cada producto debe tener asignada la liquidación persistida");
            }
        }

        /**
         * Test de segregación de liquidaciones por múltiples vendedores dentro del mismo remate.
         * <p>
         * Cada vendedor debe recibir su liquidación individual independiente con su propia
         * suma de ventas y deducción de comisión.
         * </p>
         */
        @Test
        @DisplayName("Debe generar liquidaciones independientes y cálculos separados por cada vendedor")
        void generateLiquidations_whenMultipleSellersInSameAuction_shouldSegregateLiquidations() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long auctionId = 1L;

            Client sellerB = new Client();
            sellerB.setId(20L);
            sellerB.setName("María");
            sellerB.setLastName("López");
            sellerB.setActive(true);

            // Productos de Vendedor A (defaultSeller): $4,000.00
            Product p1 = createProduct(1L, "Lote A1", 1, new BigDecimal("4000.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            // Productos de Vendedor B: $6,000.00 + $2,000.00 = $8,000.00
            Product p2 = createProduct(2L, "Lote B1", 2, new BigDecimal("6000.00"), ProductStatus.SOLD, sellerB, defaultAuction);
            Product p3 = createProduct(3L, "Lote B2", 3, new BigDecimal("2000.00"), ProductStatus.SOLD, sellerB, defaultAuction);

            when(liquidationRepository.existsById(auctionId)).thenReturn(false);
            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Arrays.asList(p1, p2, p3));
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // =================================================================
            // ACT (When)
            // =================================================================
            liquidationService.generateLiquidationsForAuction(auctionId);

            // =================================================================
            // ASSERT (Then)
            // =================================================================
            verify(liquidationRepository, times(2)).save(liquidationCaptor.capture());
            List<Liquidation> capturedLiquidations = liquidationCaptor.getAllValues();

            assertEquals(2, capturedLiquidations.size(), "Deben generarse exactamente 2 liquidaciones");

            // Localizar liquidación de seller A y seller B
            Liquidation liqSellerA = capturedLiquidations.stream()
                    .filter(l -> l.getClient().getId().equals(defaultSeller.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No se encontró liquidación para Vendedor A"));

            Liquidation liqSellerB = capturedLiquidations.stream()
                    .filter(l -> l.getClient().getId().equals(sellerB.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No se encontró liquidación para Vendedor B"));

            // Validar matemáticas Vendedor A: $4,000.00 -> Com: $400.00 -> Net: $3,600.00
            assertEquals(0, liqSellerA.getTotalSold().compareTo(new BigDecimal("4000.00")));
            assertEquals(0, liqSellerA.getRetainedCommission().compareTo(new BigDecimal("400.00")));
            assertEquals(0, liqSellerA.getNetToPay().compareTo(new BigDecimal("3600.00")));

            // Validar matemáticas Vendedor B: $8,000.00 -> Com: $800.00 -> Net: $7,200.00
            assertEquals(0, liqSellerB.getTotalSold().compareTo(new BigDecimal("8000.00")));
            assertEquals(0, liqSellerB.getRetainedCommission().compareTo(new BigDecimal("800.00")));
            assertEquals(0, liqSellerB.getNetToPay().compareTo(new BigDecimal("7200.00")));
        }
    }

    // =========================================================================
    // 2. MONTOS EN CERO Y CASOS BORDE DE VENTAS
    // =========================================================================

    @Nested
    @DisplayName("Pruebas de Montos en Cero y Ausencia de Ventas")
    class ZeroAmountAndEdgeCasesTests {

        /**
         * Caso Borde Obligatorio: Productos vendidos a $0.00 (Lotes bonificados o con precio cero).
         * <p>
         * Valida que no ocurra división por cero ni excepciones aritméticas y que
         * los importes resultantes sean $0.00 con estado PENDING.
         * </p>
         */
        @Test
        @DisplayName("Debe procesar correctamente liquidación cuando los lotes se vendieron a $0.00")
        void generateLiquidations_whenProductsSoldAtZero_shouldGenerateZeroAmountsLiquidation() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long auctionId = 1L;

            // Dos lotes con valor de venta $0.00
            Product zeroLot1 = createProduct(201L, "Lote Donación 1", 1, BigDecimal.ZERO, ProductStatus.SOLD, defaultSeller, defaultAuction);
            Product zeroLot2 = createProduct(202L, "Lote Donación 2", 2, new BigDecimal("0.00"), ProductStatus.SOLD, defaultSeller, defaultAuction);

            when(liquidationRepository.existsById(auctionId)).thenReturn(false);
            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Arrays.asList(zeroLot1, zeroLot2));
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // =================================================================
            // ACT (When)
            // =================================================================
            liquidationService.generateLiquidationsForAuction(auctionId);

            // =================================================================
            // ASSERT (Then)
            // =================================================================
            verify(liquidationRepository, times(1)).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            // Comparaciones monetarias estrictas con compareTo() contra BigDecimal.ZERO
            assertEquals(0, savedLiquidation.getTotalSold().compareTo(BigDecimal.ZERO),
                    "El total vendido de lotes a $0 debe ser numéricamente igual a 0");
            assertEquals(0, savedLiquidation.getRetainedCommission().compareTo(BigDecimal.ZERO),
                    "La comisión de una venta en $0 debe ser 0");
            assertEquals(0, savedLiquidation.getNetToPay().compareTo(BigDecimal.ZERO),
                    "El neto a pagar debe ser 0");
            assertEquals(LiquidationStatus.PENDING, savedLiquidation.getStatus(),
                    "La liquidación en $0 debe crearse en estado PENDING para su posterior auditoría o pago");
        }

        /**
         * Caso Borde Obligatorio: Remate sin productos vendidos (no hubo ventas).
         * <p>
         * Regla de Negocio:
         * Si no hay productos vendidos en el remate, se debe abortar la operación lanzando
         * RuntimeException y GARANTIZAR que NO se persista ninguna liquidación huérfana en base de datos.
         * </p>
         */
        @Test
        @DisplayName("Debe lanzar excepción y no persistir liquidaciones si no hubo ventas en el remate")
        void generateLiquidations_whenNoProductsSold_shouldThrowExceptionAndNotPersist() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long auctionId = 1L;

            when(liquidationRepository.existsById(auctionId)).thenReturn(false);
            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(defaultAuction));
            // Lista vacía de productos vendidos
            when(productRepository.findByAuctionIdAndStatus(auctionId, ProductStatus.SOLD))
                    .thenReturn(Collections.emptyList());

            // =================================================================
            // ACT & ASSERT (When & Then)
            // =================================================================
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    liquidationService.generateLiquidationsForAuction(auctionId)
            );

            assertEquals("No hay productos vendidos en este remate para liquidar.", exception.getMessage(),
                    "El mensaje de error de negocio debe coincidir exactamente");

            // Verificación crítica de no efectos colaterales (Clean State)
            verify(liquidationRepository, never()).save(any(Liquidation.class));
            verify(productRepository, never()).saveAll(any());
        }

        /**
         * Control de Idempotencia en Generación:
         * Impide regenerar liquidaciones si ya fueron generadas previamente para el remate.
         */
        @Test
        @DisplayName("Debe lanzar excepción si las liquidaciones para el remate ya existen (Idempotencia de Generación)")
        void generateLiquidations_whenAlreadyGenerated_shouldThrowExceptionImmediately() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long auctionId = 1L;
            when(liquidationRepository.existsById(auctionId)).thenReturn(true);

            // =================================================================
            // ACT & ASSERT (When & Then)
            // =================================================================
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    liquidationService.generateLiquidationsForAuction(auctionId)
            );

            assertEquals("Las liquidaciones para este remate ya fueron generadas.", exception.getMessage());

            // Garantizar que no se ejecutaron consultas adicionales innecesarias
            verify(auctionRepository, never()).findById(any());
            verify(productRepository, never()).findByAuctionIdAndStatus(any(), any());
            verify(liquidationRepository, never()).save(any());
        }

        /**
         * Manejo de remate inexistente.
         */
        @Test
        @DisplayName("Debe lanzar excepción si el remate especificado no existe")
        void generateLiquidations_whenAuctionNotFound_shouldThrowException() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long nonExistentAuctionId = 999L;
            when(liquidationRepository.existsById(nonExistentAuctionId)).thenReturn(false);
            when(auctionRepository.findById(nonExistentAuctionId)).thenReturn(Optional.empty());

            // =================================================================
            // ACT & ASSERT (When & Then)
            // =================================================================
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    liquidationService.generateLiquidationsForAuction(nonExistentAuctionId)
            );

            assertEquals("Remate no encontrado", exception.getMessage());
            verify(productRepository, never()).findByAuctionIdAndStatus(any(), any());
            verify(liquidationRepository, never()).save(any());
        }
    }

    // =========================================================================
    // 3. FLUJO DE PAGO, TRANSICIÓN DE ESTADOS E IDEMPOTENCIA
    // =========================================================================

    @Nested
    @DisplayName("Pruebas del Flujo de Pago y Transiciones de Estado")
    class PaymentFlowTests {

        /**
         * Caso Borde Obligatorio: Cambio de Estado Seguro (PENDING -> PAID).
         * <p>
         * Valida que al ejecutar el método de pago sobre una liquidación pendiente,
         * su estado mute correctamente a PAID y se persista el cambio.
         * </p>
         */
        @Test
        @DisplayName("Debe realizar la transición segura de estado PENDING a PAID y persistir el cambio")
        void markAsPaid_whenLiquidationIsPending_shouldTransitionToPaidSuccessfully() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long liquidationId = 50L;
            Liquidation pendingLiquidation = new Liquidation();
            pendingLiquidation.setId(liquidationId);
            pendingLiquidation.setStatus(LiquidationStatus.PENDING);
            pendingLiquidation.setTotalSold(new BigDecimal("10000.00"));
            pendingLiquidation.setRetainedCommission(new BigDecimal("1000.00"));
            pendingLiquidation.setNetToPay(new BigDecimal("9000.00"));

            when(liquidationRepository.findById(liquidationId)).thenReturn(Optional.of(pendingLiquidation));
            when(liquidationRepository.save(any(Liquidation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // =================================================================
            // ACT (When)
            // =================================================================
            liquidationService.markAsPaid(liquidationId);

            // =================================================================
            // ASSERT (Then)
            // =================================================================
            verify(liquidationRepository, times(1)).save(liquidationCaptor.capture());
            Liquidation savedLiquidation = liquidationCaptor.getValue();

            assertNotNull(savedLiquidation);
            assertEquals(LiquidationStatus.PAID, savedLiquidation.getStatus(),
                    "El estado de la liquidación debe mutar a PAID");
            assertSame(pendingLiquidation, savedLiquidation,
                    "La entidad persistida debe ser la misma instancia gestionada");
        }

        /**
         * Caso Borde Obligatorio: Doble Pago / Idempotencia (PAID -> Error).
         * <p>
         * Regla Crítica de Arquitectura Financiera:
         * Un registro ya pagado NO puede ser pagado dos veces. Debe rechazar la operación
         * mediante una excepción de negocio e impedir cualquier llamada de guardado en el repositorio.
         * </p>
         */
        @Test
        @DisplayName("Debe rechazar el doble pago lanzando excepción si la liquidación ya estaba PAID (Idempotencia)")
        void markAsPaid_whenLiquidationAlreadyPaid_shouldThrowExceptionAndPreventDuplicatePayment() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long liquidationId = 50L;
            Liquidation alreadyPaidLiquidation = new Liquidation();
            alreadyPaidLiquidation.setId(liquidationId);
            alreadyPaidLiquidation.setStatus(LiquidationStatus.PAID);
            alreadyPaidLiquidation.setTotalSold(new BigDecimal("5000.00"));
            alreadyPaidLiquidation.setRetainedCommission(new BigDecimal("500.00"));
            alreadyPaidLiquidation.setNetToPay(new BigDecimal("4500.00"));

            when(liquidationRepository.findById(liquidationId)).thenReturn(Optional.of(alreadyPaidLiquidation));

            // =================================================================
            // ACT & ASSERT (When & Then)
            // =================================================================
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    liquidationService.markAsPaid(liquidationId)
            );

            assertEquals("Esta liquidación ya fue marcada como pagada.", exception.getMessage(),
                    "Debe informar con claridad que la liquidación ya se encuentra pagada");

            // Verificación crítica: NUNCA se debe invocar save() para evitar escrituras espurias o re-auditorías falsas
            verify(liquidationRepository, never()).save(any(Liquidation.class));
            assertEquals(LiquidationStatus.PAID, alreadyPaidLiquidation.getStatus(),
                    "El estado no debe corromperse tras la operación fallida");
        }

        /**
         * Manejo de error al intentar pagar una liquidación que no existe en el sistema.
         */
        @Test
        @DisplayName("Debe lanzar excepción al intentar pagar una liquidación inexistente")
        void markAsPaid_whenLiquidationNotFound_shouldThrowException() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
            Long nonExistentId = 9999L;
            when(liquidationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // =================================================================
            // ACT & ASSERT (When & Then)
            // =================================================================
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    liquidationService.markAsPaid(nonExistentId)
            );

            assertEquals("Liquidación no encontrada", exception.getMessage());
            verify(liquidationRepository, never()).save(any());
        }
    }

    // =========================================================================
    // 4. RESUMEN FINANCIERO Y AGREGACIONES
    // =========================================================================

    @Nested
    @DisplayName("Pruebas de Resumen y Agregación Financiera")
    class SummaryAggregationTests {

        /**
         * Valida que la agregación del resumen contable por remate preserve la exactitud matemática.
         */
        @Test
        @DisplayName("Debe acumular ventas, comisiones y netos con precisión matemática absoluta en el resumen")
        void getSummaryByAuction_shouldAggregateTotalsAndCommissionsAccurately() {
            // =================================================================
            // ARRANGE (Given)
            // =================================================================
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

            BigDecimal expectedTotalSold = new BigDecimal("20500.50");
            BigDecimal expectedTotalCommission = new BigDecimal("2050.05");
            BigDecimal expectedTotalNet = new BigDecimal("18450.45");

            // =================================================================
            // ACT (When)
            // =================================================================
            LiquidationSummaryDTO summary = liquidationService.getSummaryByAuction(auctionId);

            // =================================================================
            // ASSERT (Then)
            // =================================================================
            assertNotNull(summary);
            assertEquals(0, summary.getTotalSold().compareTo(expectedTotalSold),
                    "El total vendido acumulado debe coincidir");
            assertEquals(0, summary.getTotalCommission().compareTo(expectedTotalCommission),
                    "La comisión total acumulada debe coincidir");
            assertEquals(0, summary.getTotalNetToPay().compareTo(expectedTotalNet),
                    "El neto acumulado debe coincidir");

            // Verificación de ecuación fundamental de balance
            BigDecimal calculatedNet = summary.getTotalSold().subtract(summary.getTotalCommission());
            assertEquals(0, calculatedNet.compareTo(summary.getTotalNetToPay()),
                    "El balance neto acumulado debe coincidir con TotalVendido - TotalComision");
        }
    }

    // =========================================================================
    // MÉTODOS DE UTILIDAD / FIXTURES
    // =========================================================================

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
