package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.FeeRecord;
import com.library.entity.Reader;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FeeRecordRepository;
import com.library.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    public Map<String, Object> getCollectionStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
        LocalDateTime endTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        result.put("totalBooks", bookRepository.count());
        result.put("availableBooks", bookRepository.countAvailableBooks());
        result.put("totalQuantity", bookRepository.sumTotalQuantity());
        result.put("availableQuantity", bookRepository.sumAvailableQuantity());
        result.put("newBooksCount", bookRepository.countByCreateTimeRange(startTime, endTime));

        List<Object[]> categoryStats = bookRepository.countByCategory();
        List<Map<String, Object>> categoryList = new ArrayList<>();
        long totalQty = bookRepository.sumTotalQuantity();
        for (Object[] stat : categoryStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", stat[0]);
            item.put("bookCount", stat[1]);
            item.put("quantity", stat[2]);
            item.put("percentage", totalQty > 0 ?
                    Math.round(((Long) stat[2]) * 100.0 / totalQty * 100) / 100.0 : 0);
            categoryList.add(item);
        }
        result.put("categoryDistribution", categoryList);

        Pageable pageable = PageRequest.of(0, 100);
        List<Book> inactiveBooks = bookRepository.findInactiveBooks(
                LocalDate.now().minusMonths(6), LocalDate.now(), pageable);
        result.put("inactiveBooksCount", inactiveBooks.size());
        result.put("inactiveBooks", inactiveBooks);

        Map<String, Integer> resourceTypeStats = new HashMap<>();
        resourceTypeStats.put("图书", (int) bookRepository.count());
        result.put("resourceTypeDistribution", resourceTypeStats);

        List<Book> neverBorrowed = bookRepository.findNeverBorrowedBooks();
        result.put("neverBorrowedCount", neverBorrowed.size());

        return result;
    }

    public Map<String, Object> getBorrowStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();

        long totalBorrows = borrowRecordRepository.countByBorrowDateRange(actualStart, actualEnd);
        long totalReturns = borrowRecordRepository.countByReturnDateRange(actualStart, actualEnd);
        long overdueCount = borrowRecordRepository.countOverdueByDateRange(actualStart, actualEnd);
        long renewCount = borrowRecordRepository.countRenewByDateRange(actualStart, actualEnd);

        result.put("totalBorrows", totalBorrows);
        result.put("totalReturns", totalReturns);
        result.put("overdueCount", overdueCount);
        result.put("renewCount", renewCount);
        result.put("returnRate", totalBorrows > 0 ?
                Math.round(totalReturns * 100.0 / totalBorrows * 100) / 100.0 : 0);
        result.put("overdueRate", totalBorrows > 0 ?
                Math.round(overdueCount * 100.0 / totalBorrows * 100) / 100.0 : 0);
        result.put("renewRate", totalBorrows > 0 ?
                Math.round(renewCount * 100.0 / totalBorrows * 100) / 100.0 : 0);
        result.put("reservationSuccessRate", 100.0);

        long days = ChronoUnit.DAYS.between(actualStart, actualEnd) + 1;
        if (days <= 31) {
            result.put("dailyBorrows", formatTimeSeriesData(
                    borrowRecordRepository.countDailyBorrows(actualStart, actualEnd), "date"));
        } else if (days <= 180) {
            result.put("weeklyBorrows", formatTimeSeriesData(
                    borrowRecordRepository.countWeeklyBorrows(actualStart, actualEnd), "week"));
        } else {
            result.put("monthlyBorrows", formatTimeSeriesData(
                    borrowRecordRepository.countMonthlyBorrows(actualStart, actualEnd), "month"));
        }

        LocalDate today = LocalDate.now();
        result.put("todayBorrows", borrowRecordRepository.countByBorrowDateRange(today, today));
        result.put("weekBorrows", borrowRecordRepository.countByBorrowDateRange(
                today.minusDays(7), today));
        result.put("monthBorrows", borrowRecordRepository.countByBorrowDateRange(
                today.withDayOfMonth(1), today));
        result.put("yearBorrows", borrowRecordRepository.countByBorrowDateRange(
                today.withDayOfYear(1), today));

        return result;
    }

    public Map<String, Object> getReaderStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();
        LocalDateTime startTime = actualStart.atStartOfDay();
        LocalDateTime endTime = actualEnd.atTime(23, 59, 59);

        result.put("totalReaders", readerRepository.count());
        result.put("activeReaders", readerRepository.countActiveReaders());
        result.put("newReadersCount", readerRepository.countByCreateTimeRange(startTime, endTime));

        Pageable pageable = PageRequest.of(0, 20);
        List<Object[]> activeReaderList = borrowRecordRepository.findActiveReaders(
                actualStart, actualEnd, pageable);
        List<Map<String, Object>> activeReaders = new ArrayList<>();
        for (Object[] arr : activeReaderList) {
            Map<String, Object> item = new HashMap<>();
            item.put("readerId", arr[0]);
            item.put("readerName", arr[1]);
            item.put("borrowCount", arr[2]);
            activeReaders.add(item);
        }
        result.put("activeReaderList", activeReaders);
        result.put("activeReaderCount", activeReaders.size());

        List<Object[]> identityStats = readerRepository.countByIdentityType();
        List<Map<String, Object>> identityList = new ArrayList<>();
        for (Object[] stat : identityStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("identityType", stat[0]);
            item.put("count", stat[1]);
            identityList.add(item);
        }
        result.put("identityDistribution", identityList);

        List<Object[]> ageStats = readerRepository.countByAgeGroup();
        List<Map<String, Object>> ageList = new ArrayList<>();
        for (Object[] stat : ageStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("ageGroup", stat[0]);
            item.put("count", stat[1]);
            ageList.add(item);
        }
        result.put("ageDistribution", ageList);

        List<Object[]> creditStats = readerRepository.countByCreditLevel();
        List<Map<String, Object>> creditList = new ArrayList<>();
        for (Object[] stat : creditStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("creditLevel", stat[0]);
            item.put("count", stat[1]);
            creditList.add(item);
        }
        result.put("creditDistribution", creditList);

        result.put("readersWithViolations", readerRepository.countReadersWithViolations());

        List<Object[]> preferenceByIdentity = readerRepository.findReaderPreferenceByIdentity(
                actualStart, actualEnd);
        result.put("preferenceByIdentity", formatPreferenceData(preferenceByIdentity, "identityType"));

        List<Object[]> preferenceByAge = readerRepository.findReaderPreferenceByAge(
                actualStart, actualEnd);
        result.put("preferenceByAge", formatPreferenceData(preferenceByAge, "ageGroup"));

        return result;
    }

    public Map<String, Object> getHotResourcesStatistics(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Object> result = new HashMap<>();

        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();

        Pageable topPageable = PageRequest.of(0, limit);
        List<Object[]> hotBooks = bookRepository.findHotBooks(actualStart, actualEnd, topPageable);
        List<Map<String, Object>> hotBookList = new ArrayList<>();
        int rank = 1;
        for (Object[] book : hotBooks) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("bookId", book[0]);
            item.put("bookName", book[1]);
            item.put("borrowCount", book[2]);
            hotBookList.add(item);
        }
        result.put("hotBooks", hotBookList);

        List<Object[]> coldBooks = bookRepository.findColdBooks(actualStart, actualEnd, topPageable);
        List<Map<String, Object>> coldBookList = new ArrayList<>();
        rank = 1;
        for (Object[] book : coldBooks) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("bookId", book[0]);
            item.put("bookName", book[1]);
            item.put("borrowCount", book[2]);
            coldBookList.add(item);
        }
        result.put("coldBooks", coldBookList);

        List<Object[]> hotCategories = bookRepository.findHotCategories(actualStart, actualEnd);
        List<Map<String, Object>> hotCategoryList = new ArrayList<>();
        for (Object[] category : hotCategories) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", category[0]);
            item.put("borrowCount", category[1]);
            hotCategoryList.add(item);
        }
        result.put("hotCategories", hotCategoryList);

        return result;
    }

    public Map<String, Object> getFeeStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();
        LocalDateTime startTime = actualStart.atStartOfDay();
        LocalDateTime endTime = actualEnd.atTime(23, 59, 59);

        BigDecimal totalFine = borrowRecordRepository.sumFineAmountByDateRange(actualStart, actualEnd);
        BigDecimal totalCompensation = borrowRecordRepository.sumCompensationAmountByDateRange(
                actualStart, actualEnd);

        result.put("totalFine", totalFine);
        result.put("totalCompensation", totalCompensation);
        result.put("totalIncome", totalFine.add(totalCompensation));

        List<Object[]> feeTypeStats = feeRecordRepository.sumAmountByFeeTypeAndDateRange(
                startTime, endTime);
        List<Map<String, Object>> feeTypeList = new ArrayList<>();
        for (Object[] stat : feeTypeStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("feeType", stat[0]);
            item.put("amount", stat[1]);
            feeTypeList.add(item);
        }
        result.put("feeTypeDistribution", feeTypeList);

        result.put("unpaidAmount", feeRecordRepository.sumUnpaidAmount());
        result.put("unpaidCount", feeRecordRepository.countUnpaidRecords());

        List<FeeRecord> unpaidRecords = feeRecordRepository.findUnpaidRecords();
        List<Map<String, Object>> unpaidList = new ArrayList<>();
        for (FeeRecord record : unpaidRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("readerName", record.getReader() != null ? record.getReader().getName() : "");
            item.put("feeType", record.getFeeType());
            item.put("amount", record.getAmount());
            item.put("paidAmount", record.getPaidAmount());
            item.put("unpaidAmount", record.getAmount().subtract(record.getPaidAmount()));
            item.put("createTime", record.getCreateTime());
            unpaidList.add(item);
        }
        result.put("unpaidRecords", unpaidList);

        List<FeeRecord> feeRecords = feeRecordRepository.findByDateRange(startTime, endTime);
        List<Map<String, Object>> feeDetailList = new ArrayList<>();
        for (FeeRecord record : feeRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("readerName", record.getReader() != null ? record.getReader().getName() : "");
            item.put("feeType", record.getFeeType());
            item.put("amount", record.getAmount());
            item.put("paidAmount", record.getPaidAmount());
            item.put("isPaid", record.getIsPaid());
            item.put("remark", record.getRemark());
            item.put("createTime", record.getCreateTime());
            feeDetailList.add(item);
        }
        result.put("feeDetails", feeDetailList);

        return result;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        result.put("totalBooks", bookRepository.count());
        result.put("totalReaders", readerRepository.count());
        result.put("borrowingCount", borrowRecordRepository.countBorrowing());
        result.put("overdueCount", borrowRecordRepository.countOverdue());
        result.put("todayBorrows", borrowRecordRepository.countByBorrowDateRange(today, today));
        result.put("monthBorrows", borrowRecordRepository.countByBorrowDateRange(
                today.withDayOfMonth(1), today));

        LocalDate startDate = LocalDate.now().minusMonths(6);
        LocalDate endDate = LocalDate.now();
        Pageable topPageable = PageRequest.of(0, 10);
        List<Object[]> hotBooks = bookRepository.findHotBooks(startDate, endDate, topPageable);
        List<Map<String, Object>> hotBookList = new ArrayList<>();
        for (Object[] book : hotBooks) {
            Map<String, Object> item = new HashMap<>();
            item.put("bookName", book[1]);
            item.put("borrowCount", book[2]);
            hotBookList.add(item);
        }
        result.put("hotBooks", hotBookList);

        return result;
    }

    public List<BorrowRecord> getBorrowRecordsForExport(LocalDate startDate, LocalDate endDate) {
        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();
        return borrowRecordRepository.findByBorrowDateRange(actualStart, actualEnd);
    }

    public List<Book> getBooksForExport() {
        return bookRepository.findAll();
    }

    public List<Reader> getReadersForExport() {
        return readerRepository.findAll();
    }

    public List<FeeRecord> getFeeRecordsForExport(LocalDate startDate, LocalDate endDate) {
        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();
        return feeRecordRepository.findByDateRange(actualStart.atStartOfDay(), actualEnd.atTime(23, 59, 59));
    }

    private List<Map<String, Object>> formatTimeSeriesData(List<Object[]> data, String timeKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] item : data) {
            Map<String, Object> map = new HashMap<>();
            map.put(timeKey, item[0]);
            map.put("count", item[1]);
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> formatPreferenceData(List<Object[]> data, String groupKey) {
        Map<String, Map<String, Object>> groupedData = new LinkedHashMap<>();
        for (Object[] item : data) {
            String group = (String) item[0];
            String category = (String) item[1];
            Long count = (Long) item[2];

            groupedData.computeIfAbsent(group, k -> {
                Map<String, Object> map = new HashMap<>();
                map.put(groupKey, k);
                map.put("categories", new ArrayList<Map<String, Object>>());
                return map;
            });

            Map<String, Object> categoryItem = new HashMap<>();
            categoryItem.put("category", category);
            categoryItem.put("count", count);
            ((List<Map<String, Object>>) groupedData.get(group).get("categories")).add(categoryItem);
        }
        return new ArrayList<>(groupedData.values());
    }

    public Map<String, Object> getAllStatisticsForScreen(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate actualStart = startDate != null ? startDate : today.minusDays(30);
        LocalDate actualEnd = endDate != null ? endDate : today;

        // Dashboard指标
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalBooks", bookRepository.count());
        dashboard.put("totalReaders", readerRepository.count());
        dashboard.put("todayBorrow", borrowRecordRepository.countByBorrowDateRange(today, today));
        dashboard.put("todayReturn", borrowRecordRepository.countByReturnDateRange(today, today));
        dashboard.put("overdueCount", borrowRecordRepository.countOverdue());
        BigDecimal totalFine = borrowRecordRepository.sumFineAmountByDateRange(
                today.withDayOfMonth(1), today);
        BigDecimal totalCompensation = borrowRecordRepository.sumCompensationAmountByDateRange(
                today.withDayOfMonth(1), today);
        dashboard.put("monthlyIncome", totalFine.add(totalCompensation));
        result.put("dashboard", dashboard);

        // 分类统计（馆藏分类占比）
        List<Object[]> categoryStats = bookRepository.countByCategory();
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Object[] stat : categoryStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", stat[0]);
            item.put("count", stat[1] != null ? ((Number) stat[1]).longValue() : 0);
            categoryList.add(item);
        }
        result.put("categoryStats", categoryList);

        // 年龄段统计
        List<Object[]> ageStats = readerRepository.countByAgeGroup();
        List<Map<String, Object>> ageList = new ArrayList<>();
        for (Object[] stat : ageStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", stat[0] != null ? stat[0].toString() : "未知");
            item.put("count", stat[1] != null ? ((Number) stat[1]).longValue() : 0);
            ageList.add(item);
        }
        result.put("ageStats", ageList);

        // 近30天借阅趋势
        Map<String, Object> trendStats = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> borrowData = new ArrayList<>();
        List<Long> returnData = new ArrayList<>();

        // 按日期统计借阅和归还
        Map<LocalDate, Long> borrowMap = new HashMap<>();
        Map<LocalDate, Long> returnMap = new HashMap<>();

        List<Object[]> dailyBorrows = borrowRecordRepository.countDailyBorrows(actualStart, actualEnd);
        for (Object[] row : dailyBorrows) {
            String dateStr = row[0].toString();
            LocalDate date = LocalDate.parse(dateStr);
            borrowMap.put(date, ((Number) row[1]).longValue());
        }

        List<Object[]> dailyReturns = borrowRecordRepository.countDailyReturns(actualStart, actualEnd);
        for (Object[] row : dailyReturns) {
            String dateStr = row[0].toString();
            LocalDate date = LocalDate.parse(dateStr);
            returnMap.put(date, ((Number) row[1]).longValue());
        }

        // 生成近30天的连续日期
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            borrowData.add(borrowMap.getOrDefault(date, 0L));
            returnData.add(returnMap.getOrDefault(date, 0L));
        }

        trendStats.put("dates", dates);
        trendStats.put("borrowData", borrowData);
        trendStats.put("returnData", returnData);
        result.put("trendStats", trendStats);

        // 热门分类借阅排行
        List<Object[]> hotCategoryStats = bookRepository.findHotCategories(actualStart, actualEnd);
        List<Map<String, Object>> hotCategoryList = new ArrayList<>();
        for (Object[] stat : hotCategoryStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", stat[0]);
            item.put("count", stat[1] != null ? ((Number) stat[1]).longValue() : 0);
            hotCategoryList.add(item);
        }
        result.put("hotCategoryStats", hotCategoryList);

        // 热门图书TOP10
        Pageable topPageable = PageRequest.of(0, limit);
        List<Object[]> hotBooks = bookRepository.findHotBooks(actualStart, actualEnd, topPageable);
        List<Map<String, Object>> hotBookList = new ArrayList<>();
        for (Object[] book : hotBooks) {
            Map<String, Object> item = new LinkedHashMap<>();
            Long bookId = ((Number) book[0]).longValue();
            item.put("id", bookId);
            item.put("name", book[1]);
            item.put("borrowCount", book[2] != null ? ((Number) book[2]).longValue() : 0);

            // 获取图书详细信息
            bookRepository.findById(bookId).ifPresent(b -> {
                item.put("author", b.getAuthor());
                item.put("categoryName", b.getCategory());
            });
            hotBookList.add(item);
        }
        Map<String, Object> hotBooksResult = new LinkedHashMap<>();
        hotBooksResult.put("list", hotBookList);
        result.put("hotBooks", hotBooksResult);

        // 实时借阅动态（最近的借阅记录）
        List<Map<String, Object>> realtimeActivities = new ArrayList<>();
        List<BorrowRecord> recentRecords = borrowRecordRepository.findRecentRecords(PageRequest.of(0, 20));
        for (BorrowRecord record : recentRecords) {
            Map<String, Object> activity = new LinkedHashMap<>();
            readerRepository.findById(record.getReaderId()).ifPresent(reader -> {
                activity.put("readerName", reader.getName());
            });
            bookRepository.findById(record.getBookId()).ifPresent(book -> {
                activity.put("bookName", book.getName());
            });
            activity.put("time", record.getCreateTime() != null ?
                    record.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            activity.put("status", record.getStatus() == 1 ? "success" : "warning");
            activity.put("statusText", record.getStatus() == 1 ? "借阅中" : "已归还");
            realtimeActivities.add(activity);
        }
        result.put("realtimeActivities", realtimeActivities);

        return result;
    }
}
