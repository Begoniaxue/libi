package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ExportService exportService;

    public Map<String, Object> generateMonthlyReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return generateReport(startDate, endDate, "月度");
    }

    public Map<String, Object> generateYearlyReport(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return generateReport(startDate, endDate, "年度");
    }

    private Map<String, Object> generateReport(LocalDate startDate, LocalDate endDate, String reportType) {
        Map<String, Object> report = new LinkedHashMap<>();

        if ("月度".equals(reportType)) {
            report.put("reportTitle", startDate.format(DateTimeFormatter.ofPattern("yyyy年MM月")) + "运营报告");
        } else {
            report.put("reportTitle", startDate.format(DateTimeFormatter.ofPattern("yyyy年")) + "运营报告");
        }
        report.put("reportType", reportType);
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, Object> overview = new LinkedHashMap<>();
        var collectionStats = statisticsService.getCollectionStatistics(startDate, endDate);
        var borrowStats = statisticsService.getBorrowStatistics(startDate, endDate);
        var readerStats = statisticsService.getReaderStatistics(startDate, endDate);
        var feeStats = statisticsService.getFeeStatistics(startDate, endDate);

        overview.put("totalBooks", collectionStats.get("totalBooks"));
        overview.put("newBooksCount", collectionStats.get("newBooksCount"));
        overview.put("totalReaders", readerStats.get("totalReaders"));
        overview.put("newReadersCount", readerStats.get("newReadersCount"));
        overview.put("totalBorrows", borrowStats.get("totalBorrows"));
        overview.put("returnRate", borrowStats.get("returnRate"));
        overview.put("overdueRate", borrowStats.get("overdueRate"));
        overview.put("totalIncome", feeStats.get("totalIncome"));
        report.put("overview", overview);

        report.put("collectionStatistics", collectionStats);
        report.put("borrowStatistics", borrowStats);
        report.put("readerStatistics", readerStats);
        var hotStats = statisticsService.getHotResourcesStatistics(startDate, endDate, 10);
        report.put("hotResourcesStatistics", hotStats);
        report.put("feeStatistics", feeStats);

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("bookGrowthRate", calculateGrowthRate(
                toLong(collectionStats.get("newBooksCount")),
                toLong(collectionStats.get("totalBooks")) - toLong(collectionStats.get("newBooksCount"))));
        analysis.put("readerGrowthRate", calculateGrowthRate(
                toLong(readerStats.get("newReadersCount")),
                toLong(readerStats.get("totalReaders")) - toLong(readerStats.get("newReadersCount"))));
        analysis.put("borrowTrend", analyzeBorrowTrend(borrowStats));
        analysis.put("suggestions", generateSuggestions(collectionStats, borrowStats, readerStats, feeStats));
        report.put("analysis", analysis);

        return report;
    }

    private long toLong(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    private double calculateGrowthRate(long current, long previous) {
        if (previous == 0) return 100.0;
        return Math.round((double) current / previous * 10000) / 100.0;
    }

    private String analyzeBorrowTrend(Map<String, Object> borrowStats) {
        double returnRate = (double) borrowStats.get("returnRate");
        double overdueRate = (double) borrowStats.get("overdueRate");

        if (overdueRate > 10) {
            return "逾期率偏高，建议加强逾期提醒措施";
        } else if (returnRate > 90 && overdueRate < 5) {
            return "借阅情况良好，读者信用整体良好";
        } else {
            return "借阅情况正常，需持续关注";
        }
    }

    private java.util.List<String> generateSuggestions(Map<String, Object> collectionStats,
                                                      Map<String, Object> borrowStats,
                                                      Map<String, Object> readerStats,
                                                      Map<String, Object> feeStats) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();

        long inactiveBooks = toLong(collectionStats.get("inactiveBooksCount"));
        if (inactiveBooks > 10) {
            suggestions.add("馆内有" + inactiveBooks + "本图书近6个月未被借阅，建议考虑剔旧或推荐");
        }

        double overdueRate = borrowStats.get("overdueRate") != null ? 
            ((Number) borrowStats.get("overdueRate")).doubleValue() : 0.0;
        if (overdueRate > 10) {
            suggestions.add("逾期率达到" + overdueRate + "%，建议加强逾期图书催还工作");
        }

        long readersWithViolations = toLong(readerStats.get("readersWithViolations"));
        if (readersWithViolations > 0) {
            suggestions.add("有" + readersWithViolations + "位读者存在违规记录，建议关注并进行信用管理");
        }

        java.math.BigDecimal unpaidAmount = feeStats.get("unpaidAmount") instanceof java.math.BigDecimal ?
            (java.math.BigDecimal) feeStats.get("unpaidAmount") : java.math.BigDecimal.ZERO;
        if (unpaidAmount != null && unpaidAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            suggestions.add("存在未结清费用" + unpaidAmount + "元，建议进行费用催收");
        }

        suggestions.add("建议定期开展读者阅读推广活动，提高图书利用率");
        suggestions.add("建议优化馆藏结构，增加热门分类图书采购");

        return suggestions;
    }

    public byte[] generateReportExcel(LocalDate startDate, LocalDate endDate, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportStatisticsToPdf("collection", startDate, endDate);
        } else {
            return exportService.exportBooksToExcel();
        }
    }

    public byte[] generateMonthlyReportFile(int year, int month, String format) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportStatisticsToPdf("collection", startDate, endDate);
        } else {
            return exportService.exportBorrowRecordsToExcel(startDate, endDate);
        }
    }

    public byte[] generateYearlyReportFile(int year, String format) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportStatisticsToPdf("collection", startDate, endDate);
        } else {
            return exportService.exportBorrowRecordsToExcel(startDate, endDate);
        }
    }
}
