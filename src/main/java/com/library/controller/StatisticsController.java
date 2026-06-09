package com.library.controller;

import com.library.common.Result;
import com.library.service.ExportService;
import com.library.service.ReportService;
import com.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private ReportService reportService;

    @GetMapping
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.putAll(statisticsService.getDashboardStatistics());
        return Result.success(result);
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(statisticsService.getDashboardStatistics());
    }

    @GetMapping("/collection")
    public Result<Map<String, Object>> getCollectionStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(statisticsService.getCollectionStatistics(startDate, endDate));
    }

    @GetMapping("/borrow")
    public Result<Map<String, Object>> getBorrowStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(statisticsService.getBorrowStatistics(startDate, endDate));
    }

    @GetMapping("/reader")
    public Result<Map<String, Object>> getReaderStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(statisticsService.getReaderStatistics(startDate, endDate));
    }

    @GetMapping("/hot-resources")
    public Result<Map<String, Object>> getHotResourcesStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getHotResourcesStatistics(startDate, endDate, limit));
    }

    @GetMapping("/fee")
    public Result<Map<String, Object>> getFeeStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(statisticsService.getFeeStatistics(startDate, endDate));
    }

    @GetMapping("/all")
    public Result<Map<String, Object>> getAllStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getAllStatisticsForScreen(startDate, endDate, limit));
    }

    @GetMapping("/export/books/excel")
    public ResponseEntity<byte[]> exportBooksToExcel() {
        byte[] data = exportService.exportBooksToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "图书数据_" + System.currentTimeMillis() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/borrow/excel")
    public ResponseEntity<byte[]> exportBorrowRecordsToExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        byte[] data = exportService.exportBorrowRecordsToExcel(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "借阅记录_" + System.currentTimeMillis() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/readers/excel")
    public ResponseEntity<byte[]> exportReadersToExcel() {
        byte[] data = exportService.exportReadersToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "读者数据_" + System.currentTimeMillis() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/fee/excel")
    public ResponseEntity<byte[]> exportFeeRecordsToExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        byte[] data = exportService.exportFeeRecordsToExcel(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "费用记录_" + System.currentTimeMillis() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/{reportType}/pdf")
    public ResponseEntity<byte[]> exportStatisticsToPdf(
            @PathVariable String reportType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        byte[] data = exportService.exportStatisticsToPdf(reportType, startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", getReportFileName(reportType) + "_" + System.currentTimeMillis() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/report/monthly")
    public Result<Map<String, Object>> generateMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(reportService.generateMonthlyReport(year, month));
    }

    @GetMapping("/report/yearly")
    public Result<Map<String, Object>> generateYearlyReport(
            @RequestParam int year) {
        return Result.success(reportService.generateYearlyReport(year));
    }

    @GetMapping("/report/monthly/download")
    public ResponseEntity<byte[]> downloadMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "excel") String format) {
        byte[] data = reportService.generateMonthlyReportFile(year, month, format);
        HttpHeaders headers = new HttpHeaders();
        String fileName = year + "年" + month + "月运营报告";
        if ("pdf".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            fileName += ".pdf";
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            fileName += ".xlsx";
        }
        headers.setContentDispositionFormData("attachment", fileName);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/report/yearly/download")
    public ResponseEntity<byte[]> downloadYearlyReport(
            @RequestParam int year,
            @RequestParam(defaultValue = "excel") String format) {
        byte[] data = reportService.generateYearlyReportFile(year, format);
        HttpHeaders headers = new HttpHeaders();
        String fileName = year + "年度运营报告";
        if ("pdf".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            fileName += ".pdf";
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            fileName += ".xlsx";
        }
        headers.setContentDispositionFormData("attachment", fileName);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private String getReportFileName(String reportType) {
        switch (reportType) {
            case "collection": return "馆藏数据统计报表";
            case "borrow": return "借阅数据统计报表";
            case "reader": return "读者数据统计报表";
            case "hot": return "热门资源统计报表";
            case "fee": return "费用数据统计报表";
            default: return "数据统计报表";
        }
    }
}
