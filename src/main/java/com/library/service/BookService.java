package com.library.service;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Map<String, Object> getBookPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Book> bookPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            bookPage = bookRepository.findByNameContainingOrAuthorContainingOrIsbnContaining(
                    keyword, keyword, keyword, pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", bookPage.getContent());
        result.put("total", bookPage.getTotalElements());
        result.put("pages", bookPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Transactional
    public Book addBook(Book book) {
        if (bookRepository.findByIsbn(book.getIsbn()) != null) {
            throw new RuntimeException("ISBN已存在");
        }
        book.setAvailableQuantity(book.getTotalQuantity());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Book book) {
        Book exist = bookRepository.findById(book.getId()).orElse(null);
        if (exist == null) {
            throw new RuntimeException("图书不存在");
        }
        if (!exist.getIsbn().equals(book.getIsbn()) && bookRepository.findByIsbn(book.getIsbn()) != null) {
            throw new RuntimeException("ISBN已存在");
        }
        int borrowed = exist.getTotalQuantity() - exist.getAvailableQuantity();
        if (book.getTotalQuantity() < borrowed) {
            throw new RuntimeException("总数量不能小于已借出数量");
        }
        book.setAvailableQuantity(book.getTotalQuantity() - borrowed);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getAvailableQuantity() < book.getTotalQuantity()) {
            throw new RuntimeException("该图书有未归还的借阅记录，无法删除");
        }
        bookRepository.deleteById(id);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookRepository.count());
        stats.put("availableBooks", bookRepository.countAvailableBooks());
        stats.put("availableQuantity", bookRepository.sumAvailableQuantity());
        return stats;
    }

    public Map<String, Object> searchBooks(int page, int size, String keyword, String name, String author,
                                          String isbn, String category, String publisher) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Book> bookPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            bookPage = bookRepository.findByFuzzySearch(keyword, pageable);
        } else if (name != null || author != null || isbn != null || category != null || publisher != null) {
            bookPage = bookRepository.findByNameContainingAndAuthorContainingAndIsbnContainingAndCategoryContainingAndPublisherContaining(
                    (name != null && !name.trim().isEmpty()) ? name : "",
                    (author != null && !author.trim().isEmpty()) ? author : "",
                    (isbn != null && !isbn.trim().isEmpty()) ? isbn : "",
                    (category != null && !category.trim().isEmpty()) ? category : "",
                    (publisher != null && !publisher.trim().isEmpty()) ? publisher : "",
                    pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", bookPage.getContent());
        result.put("total", bookPage.getTotalElements());
        result.put("pages", bookPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public List<String> getAllCategories() {
        List<Object[]> categoryStats = bookRepository.countByCategory();
        return categoryStats.stream()
                .map(row -> (String) row[0])
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .distinct()
                .toList();
    }
}
