package com.library.controller;

import com.library.common.Result;
import com.library.entity.Reader;
import com.library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    @Autowired
    private ReaderService readerService;

    @GetMapping
    public Result<Map<String, Object>> getReaderPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(readerService.getReaderPage(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Reader> getReaderById(@PathVariable Long id) {
        return Result.success(readerService.getReaderById(id));
    }

    @GetMapping("/cardNo/{cardNo}")
    public Result<Reader> getReaderByCardNo(@PathVariable String cardNo) {
        return Result.success(readerService.getReaderByCardNo(cardNo));
    }

    @GetMapping("/generate-card-no")
    public Result<String> generateCardNo() {
        return Result.success(readerService.generateCardNo());
    }

    @PostMapping
    public Result<Reader> addReader(@RequestBody Reader reader) {
        return Result.success(readerService.addReader(reader));
    }

    @PutMapping
    public Result<Reader> updateReader(@RequestBody Reader reader) {
        return Result.success(readerService.updateReader(reader));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReader(@PathVariable Long id) {
        readerService.deleteReader(id);
        return Result.success();
    }
}
