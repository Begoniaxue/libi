package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findByIsbn(String isbn);

    Page<Book> findByNameContainingOrAuthorContainingOrIsbnContaining(
            String name, String author, String isbn, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = 1 AND b.availableQuantity > 0")
    List<Book> findAvailableBooks();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = 1")
    long countAvailableBooks();

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b WHERE b.status = 1")
    long sumAvailableQuantity();
}
