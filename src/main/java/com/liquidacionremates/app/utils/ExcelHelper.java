package com.liquidacionremates.app.utils;

import com.liquidacionremates.app.dto.ProductDTO;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.exception.InvalidExcelException;
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
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                if (isRowEmpty(currentRow)) {
                    continue;
                }

                ProductDTO productDTO = new ProductDTO();

                String lotStr = formatter.formatCellValue(currentRow.getCell(0)).trim();
                if (!lotStr.isEmpty()) {
                    try {
                        productDTO.setLotNumber(Integer.parseInt(lotStr.replaceAll("[^0-9]", "")));
                    } catch (NumberFormatException e) {
                        productDTO.setLotNumber(null);
                    }
                }

                Cell nameCell = currentRow.getCell(1);
                if (nameCell != null) {
                    productDTO.setName(formatter.formatCellValue(nameCell).trim());
                }

                String priceStr = formatter.formatCellValue(currentRow.getCell(2)).trim()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", ".");
                if (!priceStr.isEmpty()) {
                    try {
                        productDTO.setBasePrice(new BigDecimal(priceStr));
                    } catch (NumberFormatException e) {
                        productDTO.setBasePrice(BigDecimal.ZERO);
                    }
                } else {
                    productDTO.setBasePrice(BigDecimal.ZERO);
                }

                Cell sellerCell = currentRow.getCell(5);
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
            throw new InvalidExcelException("Error al leer el archivo Excel: " + e.getMessage());
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