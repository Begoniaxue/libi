package com.library.controller;

import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/records")
    public Result<Map<String, Object>> getRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status) {
        return Result.success(borrowService.getRecordPage(page, size, readerId, bookId, status));
    }

    @GetMapping("/records/{id}")
    public Result<BorrowRecord> getRecordById(@PathVariable Long id) {
        return Result.success(borrowService.getRecordById(id));
    }

    @GetMapping("/reader-info/{readerId}")
    public Result<Map<String, Object>> getReaderBorrowInfo(@PathVariable Long readerId) {
        return Result.success(borrowService.getReaderBorrowInfo(readerId));
    }

    @PostMapping("/borrow")
    public Result<BorrowRecord> borrowBook(@RequestParam Long bookId, @RequestParam Long readerId) {
        return Result.success(borrowService.borrowBook(bookId, readerId));
    }

    @PostMapping("/return/{recordId}")
    public Result<BorrowRecord> returnBook(@PathVariable Long recordId) {
        return Result.success(borrowService.returnBook(recordId));
    }

    @PostMapping("/check-overdue")
    public Result<Void> checkOverdue() {
        borrowService.manualCheckOverdue();
        return Result.success();
    }
}
