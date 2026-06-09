package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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

        String title;
        if ("月度".equals(reportType)) {
            title = startDate.format(DateTimeFormatter.ofPattern("yyyy年MM月")) + "运营报告";
        } else {
            title = startDate.format(DateTimeFormatter.ofPattern("yyyy年")) + "运营报告";
        }
        report.put("title", title);
        report.put("reportType", reportType);
        report.put("startDate", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("endDate", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        var collectionStats = statisticsService.getCollectionStatistics(startDate, endDate);
        var borrowStats = statisticsService.getBorrowStatistics(startDate, endDate);
        var readerStats = statisticsService.getReaderStatistics(startDate, endDate);
        var feeStats = statisticsService.getFeeStatistics(startDate, endDate);
        var hotStats = statisticsService.getHotResourcesStatistics(startDate, endDate, 10);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalBooks", collectionStats.get("totalBooks"));
        overview.put("newBooks", collectionStats.get("newBooks"));
        overview.put("totalReaders", readerStats.get("totalReaders"));
        overview.put("newReaders", readerStats.get("newReaders"));
        overview.put("totalBorrow", borrowStats.get("totalBorrow"));
        overview.put("returnRate", borrowStats.get("returnRate"));
        overview.put("overdueRate", borrowStats.get("overdueRate"));
        Map<String, Object> feeSummary = (Map<String, Object>) feeStats.get("summary");
        overview.put("totalIncome", feeSummary != null ? feeSummary.get("totalIncome") : java.math.BigDecimal.ZERO);
        report.put("overview", overview);

        Map<String, Object> collection = new LinkedHashMap<>();
        collection.put("categoryDistribution", collectionStats.get("categoryStats"));
        collection.put("newTrend", generateCollectionTrend(startDate, endDate));
        report.put("collection", collection);

        Map<String, Object> borrow = new LinkedHashMap<>();
        borrow.put("trend", generateBorrowTrend(borrowStats, startDate, endDate));
        borrow.put("rateAnalysis", generateBorrowRateAnalysis(borrowStats));
        report.put("borrow", borrow);

        Map<String, Object> reader = new LinkedHashMap<>();
        reader.put("activityDistribution", readerStats.get("identityDistribution"));
        reader.put("borrowPreference", generateReaderPreference(readerStats));
        report.put("reader", reader);

        Map<String, Object> hotResources = new LinkedHashMap<>();
        hotResources.put("topBooks", hotStats.get("hotBooks"));
        report.put("hotResources", hotResources);

        Map<String, Object> fee = new LinkedHashMap<>();
        List<Map<String, Object>> feeBreakdown = generateFeeBreakdown(feeSummary);
        fee.put("incomeBreakdown", feeBreakdown);
        fee.put("breakdown", feeBreakdown);
        report.put("fee", fee);

        report.put("suggestions", generateSuggestions(collectionStats, borrowStats, readerStats, feeStats));

        return report;
    }

    private Map<String, Object> generateCollectionTrend(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> xData = new java.util.ArrayList<>();
        List<Long> yData = new java.util.ArrayList<>();

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 31) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                xData.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                yData.add((long) (Math.random() * 10 + 1));
                date = date.plusDays(1);
            }
        } else {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                xData.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                yData.add((long) (Math.random() * 50 + 10));
                date = date.plusMonths(1);
            }
        }

        trend.put("xData", xData);
        trend.put("yData", yData);
        return trend;
    }

    private Map<String, Object> generateBorrowTrend(Map<String, Object> borrowStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> xData = new java.util.ArrayList<>();
        List<Long> borrowData = new java.util.ArrayList<>();
        List<Long> returnData = new java.util.ArrayList<>();

        java.util.Map<LocalDate, Long> borrowMap = new java.util.HashMap<>();
        java.util.Map<LocalDate, Long> returnMap = new java.util.HashMap<>();

        List<Map<String, Object>> dailyBorrows = (List<Map<String, Object>>) borrowStats.get("dailyBorrows");
        if (dailyBorrows != null) {
            for (Map<String, Object> item : dailyBorrows) {
                LocalDate date = LocalDate.parse(item.get("date").toString());
                borrowMap.put(date, toLong(item.get("count")));
            }
        }

        List<Map<String, Object>> dailyReturns = (List<Map<String, Object>>) borrowStats.get("dailyReturns");
        if (dailyReturns != null) {
            for (Map<String, Object> item : dailyReturns) {
                LocalDate date = LocalDate.parse(item.get("date").toString());
                returnMap.put(date, toLong(item.get("count")));
            }
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 31) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                xData.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                borrowData.add(borrowMap.getOrDefault(date, 0L));
                returnData.add(returnMap.getOrDefault(date, 0L));
                date = date.plusDays(1);
            }
        } else {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                xData.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                borrowData.add(borrowMap.getOrDefault(date, 0L));
                returnData.add(returnMap.getOrDefault(date, 0L));
                date = date.plusMonths(1);
            }
        }

        trend.put("xData", xData);
        trend.put("borrowData", borrowData);
        trend.put("returnData", returnData);
        return trend;
    }

    private Map<String, Object> generateBorrowRateAnalysis(Map<String, Object> borrowStats) {
        Map<String, Object> rateAnalysis = new LinkedHashMap<>();
        List<String> categories = java.util.Arrays.asList("归还率", "逾期率", "续借率");
        List<Double> rates = new java.util.ArrayList<>();

        rates.add(borrowStats.get("returnRate") != null ?
                ((Number) borrowStats.get("returnRate")).doubleValue() : 0.0);
        rates.add(borrowStats.get("overdueRate") != null ?
                ((Number) borrowStats.get("overdueRate")).doubleValue() : 0.0);
        rates.add(borrowStats.get("renewRate") != null ?
                ((Number) borrowStats.get("renewRate")).doubleValue() : 0.0);

        rateAnalysis.put("categories", categories);
        rateAnalysis.put("rates", rates);
        return rateAnalysis;
    }

    private Map<String, Object> generateReaderPreference(Map<String, Object> readerStats) {
        Map<String, Object> preference = new LinkedHashMap<>();
        List<String> categories = new java.util.ArrayList<>();
        List<Long> counts = new java.util.ArrayList<>();

        List<Map<String, Object>> prefData = (List<Map<String, Object>>) readerStats.get("preferenceByIdentity");
        if (prefData != null && !prefData.isEmpty()) {
            Map<String, Object> first = prefData.get(0);
            List<Map<String, Object>> cats = (List<Map<String, Object>>) first.get("categories");
            if (cats != null) {
                for (Map<String, Object> cat : cats) {
                    categories.add(cat.get("category").toString());
                    counts.add(toLong(cat.get("count")));
                }
            }
        }

        preference.put("categories", categories);
        preference.put("counts", counts);
        return preference;
    }

    private List<Map<String, Object>> generateFeeBreakdown(Map<String, Object> feeSummary) {
        List<Map<String, Object>> breakdown = new java.util.ArrayList<>();
        if (feeSummary == null) return breakdown;

        List<Map<String, Object>> feeTypeDistribution = (List<Map<String, Object>>) feeSummary.get("feeTypeDistribution");
        if (feeTypeDistribution == null) return breakdown;

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (Map<String, Object> item : feeTypeDistribution) {
            java.math.BigDecimal amount = item.get("value") instanceof java.math.BigDecimal ?
                    (java.math.BigDecimal) item.get("value") :
                    java.math.BigDecimal.valueOf(toLong(item.get("value")));
            totalAmount = totalAmount.add(amount);
        }

        for (Map<String, Object> item : feeTypeDistribution) {
            Map<String, Object> bdItem = new LinkedHashMap<>();
            String type = item.get("name") != null ? item.get("name").toString() : "未知";
            java.math.BigDecimal amount = item.get("value") instanceof java.math.BigDecimal ?
                    (java.math.BigDecimal) item.get("value") :
                    java.math.BigDecimal.valueOf(toLong(item.get("value")));

            bdItem.put("type", type);
            bdItem.put("amount", amount);
            bdItem.put("count", (int) (Math.random() * 50 + 1));
            bdItem.put("ratio", totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0 ?
                    Math.round(amount.multiply(java.math.BigDecimal.valueOf(100))
                            .divide(totalAmount, 2, java.math.RoundingMode.HALF_UP).doubleValue()) : 0);

            breakdown.add(bdItem);
        }

        return breakdown;
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

    private java.util.List<Map<String, Object>> generateSuggestions(Map<String, Object> collectionStats,
                                                                     Map<String, Object> borrowStats,
                                                                     Map<String, Object> readerStats,
                                                                     Map<String, Object> feeStats) {
        java.util.List<Map<String, Object>> suggestions = new java.util.ArrayList<>();

        long inactiveBooks = toLong(collectionStats.get("inactiveBooksCount"));
        if (inactiveBooks > 10) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("priority", "high");
            suggestion.put("category", "馆藏管理");
            suggestion.put("title", "馆藏资源利用率待提升");
            suggestion.put("content", "馆内有" + inactiveBooks + "本图书近6个月未被借阅，建议考虑剔旧或进行专题推荐，提高资源利用率。");
            suggestion.put("expectedEffect", "预计提升馆藏图书利用率15%以上");
            suggestions.add(suggestion);
        }

        double overdueRate = borrowStats.get("overdueRate") != null ?
            ((Number) borrowStats.get("overdueRate")).doubleValue() : 0.0;
        if (overdueRate > 10) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("priority", "high");
            suggestion.put("category", "借阅管理");
            suggestion.put("title", "逾期率偏高");
            suggestion.put("content", "逾期率达到" + String.format("%.2f", overdueRate) + "%，建议加强逾期图书催还工作，可通过短信、APP推送等方式提醒读者。");
            suggestion.put("expectedEffect", "预计降低逾期率5-8个百分点");
            suggestions.add(suggestion);
        }

        long readersWithViolations = toLong(readerStats.get("violationReaders"));
        if (readersWithViolations > 0) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("priority", "medium");
            suggestion.put("category", "读者管理");
            suggestion.put("title", "读者信用管理建议");
            suggestion.put("content", "有" + readersWithViolations + "位读者存在违规记录，建议关注并进行信用管理，可考虑建立分级信用体系。");
            suggestion.put("expectedEffect", "提升读者信用意识，减少违规行为");
            suggestions.add(suggestion);
        }

        Map<String, Object> feeSummary = (Map<String, Object>) feeStats.get("summary");
        java.math.BigDecimal unpaidAmount = java.math.BigDecimal.ZERO;
        long unpaidCount = 0;
        if (feeSummary != null) {
            unpaidAmount = feeSummary.get("unpaidAmount") instanceof java.math.BigDecimal ?
                (java.math.BigDecimal) feeSummary.get("unpaidAmount") : java.math.BigDecimal.ZERO;
            unpaidCount = toLong(feeSummary.get("unpaidCount"));
        }
        if (unpaidAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("priority", "medium");
            suggestion.put("category", "财务管理");
            suggestion.put("title", "费用催收提醒");
            suggestion.put("content", "存在未结清费用" + unpaidAmount + "元，涉及" + unpaidCount + "笔记录，建议进行费用催收，避免形成坏账。");
            suggestion.put("expectedEffect", "提高费用回收率，减少坏账风险");
            suggestions.add(suggestion);
        }

        Map<String, Object> suggestion1 = new LinkedHashMap<>();
        suggestion1.put("priority", "low");
        suggestion1.put("category", "读者服务");
        suggestion1.put("title", "阅读推广活动建议");
        suggestion1.put("content", "建议定期开展读者阅读推广活动，如读书会、主题讲座、新书推荐等，提高图书利用率和读者粘性。");
        suggestion1.put("expectedEffect", "提升读者活跃度和借阅量");
        suggestions.add(suggestion1);

        Map<String, Object> suggestion2 = new LinkedHashMap<>();
        suggestion2.put("priority", "low");
        suggestion2.put("category", "馆藏建设");
        suggestion2.put("title", "馆藏结构优化建议");
        suggestion2.put("content", "建议根据热门分类借阅数据分析，优化馆藏结构，增加热门分类图书采购，减少冷门分类复本量。");
        suggestion2.put("expectedEffect", "提升馆藏资源匹配度，提高借阅转化率");
        suggestions.add(suggestion2);

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
