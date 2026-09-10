package com.liquidacionremates.app.utils;

import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Client;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ExcelHelper {

    public List<ProductDTO> parseExcelFile(InputStream is) {
        List<ProductDTO> products = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0); // Tomamos la primera hoja del Excel
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Saltamos la primera fila si es la cabecera (Lote, Producto, etc.)
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                // Si la fila está completamente vacía, la ignoramos
                if (isRowEmpty(currentRow)) {
                    continue;
                }

                ProductDTO productDTO = new ProductDTO();

                // 1. Columna 0: Lote (Integer)
                String lotStr = formatter.formatCellValue(currentRow.getCell(0)).trim();
                if (!lotStr.isEmpty()) {
                    try {
                        productDTO.setLotNumber(Integer.parseInt(lotStr.replaceAll("[^0-9]", "")));
                    } catch (NumberFormatException e) {
                        productDTO.setLotNumber(null); // O manejar un lote por defecto
                    }
                }

                // 2. Columna 1: Nombre del producto (String)
                Cell nameCell = currentRow.getCell(1);
                if (nameCell != null) {
                    productDTO.setName(formatter.formatCellValue(nameCell).trim());
                }

                // 3. Columna 2: Precio Base (BigDecimal)
                String priceStr = formatter.formatCellValue(currentRow.getCell(2)).trim()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", "."); // Limpieza básica de formato de moneda
                if (!priceStr.isEmpty()) {
                    try {
                        productDTO.setBasePrice(new BigDecimal(priceStr));
                    } catch (NumberFormatException e) {
                        productDTO.setBasePrice(BigDecimal.ZERO);
                    }
                } else {
                    productDTO.setBasePrice(BigDecimal.ZERO);
                }

                // 4. Columna 3: Dueño / Vendedor (Guardamos el nombre temporalmente en un ClientDTO)
                Cell sellerCell = currentRow.getCell(3);
                if (sellerCell != null) {
                    String sellerName = formatter.formatCellValue(sellerCell).trim();
                    if (!sellerName.isEmpty()) {
                        Client tempClient = new Client();
                        tempClient.setName(sellerName);
                        productDTO.setSeller(tempClient);
                    }
                }

                products.add(productDTO);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al leer el archivo Excel: " + e.getMessage(), e);
        }

        return products;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}