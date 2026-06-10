package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("SELECT COALESCE(SUM(b.totalQuantity), 0) FROM Book b WHERE b.status = 1")
    long sumTotalQuantity();

    @Query("SELECT b.category, COUNT(b), COALESCE(SUM(b.totalQuantity), 0) " +
           "FROM Book b WHERE b.status = 1 GROUP BY b.category")
    List<Object[]> countByCategory();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = 1 AND b.createTime BETWEEN :startTime AND :endTime")
    long countByCreateTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Book b WHERE b.status = 1 AND b.id NOT IN " +
           "(SELECT DISTINCT br.bookId FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate)")
    List<Book> findInactiveBooks(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT br.bookId, b.name, COUNT(br) as borrowCount " +
           "FROM BorrowRecord br JOIN Book b ON br.bookId = b.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.bookId, b.name ORDER BY borrowCount DESC")
    List<Object[]> findHotBooks(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT b.category, COUNT(br) as borrowCount " +
           "FROM BorrowRecord br JOIN Book b ON br.bookId = b.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY b.category ORDER BY borrowCount DESC")
    List<Object[]> findHotCategories(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT br.bookId, b.name, COUNT(br) as borrowCount " +
           "FROM BorrowRecord br JOIN Book b ON br.bookId = b.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.bookId, b.name ORDER BY borrowCount ASC")
    List<Object[]> findColdBooks(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = 1 " +
           "AND b.id NOT IN (SELECT DISTINCT br.bookId FROM BorrowRecord br)")
    List<Book> findNeverBorrowedBooks();

    Page<Book> findByNameContainingAndAuthorContainingAndIsbnContainingAndCategoryContainingAndPublisherContaining(
            String name, String author, String isbn, String category, String publisher, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = 1 AND " +
           "(b.name LIKE %?1% OR b.author LIKE %?1% OR b.isbn LIKE %?1% OR " +
           "b.category LIKE %?1% OR b.publisher LIKE %?1%)")
    Page<Book> findByFuzzySearch(String keyword, Pageable pageable);

    List<Book> findByCategory(String category);
}
