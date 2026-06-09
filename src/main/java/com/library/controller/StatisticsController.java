package com.library.controller;

import com.library.common.Result;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReaderService readerService;

    @Autowired
    private BorrowService borrowService;

    @GetMapping
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(bookService.getStatistics());
        result.putAll(readerService.getStatistics());
        result.putAll(borrowService.getStatistics());
        return Result.success(result);
    }
}
