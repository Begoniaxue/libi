package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByReaderIdAndStatus(Long readerId, Integer status);

    List<BorrowRecord> findByBookIdAndStatus(Long bookId, Integer status);

    Page<BorrowRecord> findByReaderId(Long readerId, Pageable pageable);

    Page<BorrowRecord> findByBookId(Long bookId, Pageable pageable);

    Page<BorrowRecord> findByStatus(Integer status, Pageable pageable);

    Page<BorrowRecord> findByReaderIdAndStatus(Long readerId, Integer status, Pageable pageable);

    Page<BorrowRecord> findByBookIdAndStatus(Long bookId, Integer status, Pageable pageable);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.readerId = :readerId AND br.status = 1")
    long countBorrowingByReaderId(@Param("readerId") Long readerId);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.readerId = :readerId AND br.status = 3")
    long countOverdueByReaderId(@Param("readerId") Long readerId);

    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 1 AND br.dueDate < :today AND br.isOverdue = 0")
    List<BorrowRecord> findNeedOverdueRecords(@Param("today") LocalDate today);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 1")
    long countBorrowing();

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 3")
    long countOverdue();

    @Modifying
    @Transactional
    @Query("UPDATE BorrowRecord br SET br.isOverdue = 1, br.status = 3 WHERE br.id = :id")
    int markAsOverdue(@Param("id") Long id);

    boolean existsByBookIdAndReaderIdAndStatus(Long bookId, Long readerId, Integer status);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    long countByBorrowDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.returnDate BETWEEN :startDate AND :endDate")
    long countByReturnDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.isOverdue = 1 AND br.borrowDate BETWEEN :startDate AND :endDate")
    long countOverdueByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.renewCount > 0 AND br.borrowDate BETWEEN :startDate AND :endDate")
    long countRenewByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%m-%d') as period, COUNT(br) " +
           "FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%m-%d') ORDER BY period")
    List<Object[]> countDailyBorrows(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%u') as period, COUNT(br) " +
           "FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%u') ORDER BY period")
    List<Object[]> countWeeklyBorrows(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%m') as period, COUNT(br) " +
           "FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', br.borrowDate, '%Y-%m') ORDER BY period")
    List<Object[]> countMonthlyBorrows(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(br.fineAmount), 0) FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    BigDecimal sumFineAmountByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(br.compensationAmount), 0) FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompensationAmountByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT br.readerId, r.name, COUNT(br) as borrowCount " +
           "FROM BorrowRecord br JOIN Reader r ON br.readerId = r.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.readerId, r.name ORDER BY borrowCount DESC")
    List<Object[]> findActiveReaders(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    List<BorrowRecord> findByBorrowDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', br.returnDate, '%Y-%m-%d') as period, COUNT(br) " +
           "FROM BorrowRecord br WHERE br.returnDate IS NOT NULL AND br.returnDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', br.returnDate, '%Y-%m-%d') ORDER BY period")
    List<Object[]> countDailyReturns(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT br FROM BorrowRecord br ORDER BY br.borrowDate DESC, br.id DESC")
    List<BorrowRecord> findRecentRecords(Pageable pageable);
}
