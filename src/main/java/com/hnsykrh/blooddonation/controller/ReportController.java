package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.BloodInventoryDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.BloodInventory;
import com.hnsykrh.blooddonation.service.ServiceException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Optional controller for OpenPDF inventory snapshot export (third-party integration).
 */
public final class ReportController {

    private final DatabaseManager databaseManager;
    private final BloodInventoryDao inventoryDao = new BloodInventoryDao();

    public ReportController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void exportInventoryPdf(Path targetFile) throws ServiceException {
        List<BloodInventory> rows;
        try (var connection = databaseManager.openConnection()) {
            rows = inventoryDao.findAll(connection);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load inventory for report: " + e.getMessage(), e);
        }
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, java.nio.file.Files.newOutputStream(targetFile));
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            document.add(new Paragraph("Blood Donation Management System", titleFont));
            document.add(new Paragraph("Inventory snapshot — "
                    + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), bodyFont));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell(headerCell("Blood type"));
            table.addCell(headerCell("Stock (mL)"));
            table.addCell(headerCell("Last updated"));
            for (BloodInventory row : rows) {
                table.addCell(bodyCell(row.getBloodType()));
                table.addCell(bodyCell(String.valueOf(row.getStockMl())));
                table.addCell(bodyCell(row.getUpdatedAt()));
            }
            document.add(table);
            document.close();
        } catch (DocumentException | IOException e) {
            throw new ServiceException("Failed to write PDF: " + e.getMessage(), e);
        }
    }

    private static PdfPCell headerCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4f);
        return cell;
    }

    private static PdfPCell bodyCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3f);
        return cell;
    }
}
