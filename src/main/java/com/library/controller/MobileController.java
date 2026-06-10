package com.library.controller;

import com.library.common.Result;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.FeeRecord;
import com.library.entity.Reader;
import com.library.repository.FeeRecordRepository;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mobile")
public class MobileController {

    @Autowired
    private ReaderService readerService;

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    @GetMapping("/reader/{cardNo}")
    public Result<Reader> getReaderByCardNo(@PathVariable String cardNo) {
        return Result.success(readerService.getReaderByCardNo(cardNo));
    }

    @GetMapping("/reader/{readerId}/info")
    public Result<Map<String, Object>> getReaderInfo(@PathVariable Long readerId) {
        Reader reader = readerService.getReaderById(readerId);
        if (reader == null) {
            return Result.error("读者不存在");
        }

        Map<String, Object> borrowInfo = borrowService.getReaderBorrowInfo(readerId);
        BigDecimal unpaidAmount = feeRecordRepository.sumUnpaidAmountByReaderId(readerId);
        long unpaidCount = feeRecordRepository.countUnpaidRecordsByReaderId(readerId);

        Map<String, Object> result = new HashMap<>();
        result.put("reader", reader);
        result.put("borrowInfo", borrowInfo);
        result.put("unpaidAmount", unpaidAmount);
        result.put("unpaidCount", unpaidCount);

        return Result.success(result);
    }

    @GetMapping("/reader/{readerId}/borrow-records")
    public Result<Map<String, Object>> getBorrowRecords(
            @PathVariable Long readerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(borrowService.getRecordPage(page, size, readerId, null, status));
    }

    @GetMapping("/reader/{readerId}/borrowing")
    public Result<Map<String, Object>> getBorrowingBooks(
            @PathVariable Long readerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(borrowService.getRecordPage(page, size, readerId, null, 1));
    }

    @GetMapping("/reader/{readerId}/overdue")
    public Result<Map<String, Object>> getOverdueRecords(
            @PathVariable Long readerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(borrowService.getRecordPage(page, size, readerId, null, 3));
    }

    @GetMapping("/reader/{readerId}/fee-records")
    public Result<Map<String, Object>> getFeeRecords(
            @PathVariable Long readerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer isPaid) {
        Map<String, Object> result;
        if (isPaid != null) {
            result = new HashMap<>();
            var pageResult = feeRecordRepository.findByReaderIdAndIsPaidOrderByCreateTimeDesc(readerId, isPaid,
                    org.springframework.data.domain.PageRequest.of(page - 1, size));
            result.put("list", pageResult.getContent());
            result.put("total", pageResult.getTotalElements());
            result.put("pages", pageResult.getTotalPages());
            result.put("current", page);
            result.put("size", size);
        } else {
            result = new HashMap<>();
            var pageResult = feeRecordRepository.findByReaderIdOrderByCreateTimeDesc(readerId,
                    org.springframework.data.domain.PageRequest.of(page - 1, size));
            result.put("list", pageResult.getContent());
            result.put("total", pageResult.getTotalElements());
            result.put("pages", pageResult.getTotalPages());
            result.put("current", page);
            result.put("size", size);
        }
        return Result.success(result);
    }

    @GetMapping("/reader/{readerId}/renew-logs")
    public Result<Map<String, Object>> getRenewLogs(
            @PathVariable Long readerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(borrowService.getRenewLogPage(page, size, readerId));
    }

    @GetMapping("/books/search")
    public Result<Map<String, Object>> searchBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String publisher) {
        return Result.success(bookService.searchBooks(page, size, keyword, name, author, isbn, category, publisher));
    }

    @GetMapping("/books/{id}")
    public Result<Book> getBookDetail(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping("/books/categories")
    public Result<List<String>> getCategories() {
        return Result.success(bookService.getAllCategories());
    }

    @PostMapping("/borrow/renew/{recordId}")
    public Result<BorrowRecord> renewBook(@PathVariable Long recordId) {
        try {
            return Result.success(borrowService.renewBook(recordId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/borrow/record/{recordId}")
    public Result<BorrowRecord> getBorrowRecordDetail(@PathVariable Long recordId) {
        return Result.success(borrowService.getRecordById(recordId));
    }
}
