package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.service.export.CsvFormat;
import dev.chojo.simplepos.service.export.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * CSV downloads of sales, cash entries and stock batches for a range of business days, both days included, in the
 * format a spreadsheet in the given UI {@code language} expects.
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/sales.csv")
    void sales(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
               @RequestParam(defaultValue = "en") String language,
               HttpServletResponse response) throws IOException {
        ExportService.requireRange(from, to);
        Writer out = open(response, "sales", from, to);
        try {
            exportService.writeSales(from, to, CsvFormat.of(language), out);
        } catch (RuntimeException | IOException e) {
            abandon(response);
            throw e;
        }
        out.close();
    }

    @GetMapping("/cash.csv")
    void cash(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
              @RequestParam(defaultValue = "en") String language,
              HttpServletResponse response) throws IOException {
        ExportService.requireRange(from, to);
        Writer out = open(response, "cash", from, to);
        try {
            exportService.writeCash(from, to, CsvFormat.of(language), out);
        } catch (RuntimeException | IOException e) {
            abandon(response);
            throw e;
        }
        out.close();
    }

    @GetMapping("/stock.csv")
    void stock(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
               @RequestParam(defaultValue = "en") String language,
               HttpServletResponse response) throws IOException {
        ExportService.requireRange(from, to);
        Writer out = open(response, "stock", from, to);
        try {
            exportService.writeStock(from, to, CsvFormat.of(language), out);
        } catch (RuntimeException | IOException e) {
            abandon(response);
            throw e;
        }
        out.close();
    }

    /**
     * Drops what was buffered for a failed export. While nothing was sent yet, the normal error answer follows;
     * afterwards the container aborts the connection, so the client sees a failed download instead of a truncated file
     * that looks complete. That abort relies on no message converter writing the error answer as {@code text/csv}
     * into the started file.
     */
    private static void abandon(HttpServletResponse response) {
        if (!response.isCommitted()) response.reset();
    }

    private static Writer open(HttpServletResponse response, String name, LocalDate from, LocalDate to) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("%s_%s_%s.csv".formatted(name, from, to))
                .build()
                .toString());
        return new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
    }
}
