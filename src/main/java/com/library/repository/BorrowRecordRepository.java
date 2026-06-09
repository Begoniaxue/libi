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

import java.time.LocalDate;
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
}
