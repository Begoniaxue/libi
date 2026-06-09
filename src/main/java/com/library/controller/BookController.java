package com.library.controller;

import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Result<Map<String, Object>> getBookPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(bookService.getBookPage(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Book> getBookById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    public Result<Book> getBookByIsbn(@PathVariable String isbn) {
        return Result.success(bookService.getBookByIsbn(isbn));
    }

    @GetMapping("/available")
    public Result<List<Book>> getAvailableBooks() {
        return Result.success(bookService.getAvailableBooks());
    }

    @PostMapping
    public Result<Book> addBook(@RequestBody Book book) {
        return Result.success(bookService.addBook(book));
    }

    @PutMapping
    public Result<Book> updateBook(@RequestBody Book book) {
        return Result.success(bookService.updateBook(book));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }
}
