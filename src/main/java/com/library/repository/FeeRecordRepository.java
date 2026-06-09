package com.library.repository;

import com.library.entity.FeeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {

    Page<FeeRecord> findByReaderId(Long readerId, Pageable pageable);

    Page<FeeRecord> findByFeeType(String feeType, Pageable pageable);

    Page<FeeRecord> findByIsPaid(Integer isPaid, Pageable pageable);

    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FeeRecord fr WHERE fr.feeType = :feeType")
    BigDecimal sumAmountByFeeType(@Param("feeType") String feeType);

    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FeeRecord fr WHERE fr.isPaid = :isPaid")
    BigDecimal sumAmountByIsPaid(@Param("isPaid") Integer isPaid);

    @Query("SELECT COALESCE(SUM(fr.amount - fr.paidAmount), 0) FROM FeeRecord fr WHERE fr.isPaid = 0")
    BigDecimal sumUnpaidAmount();

    @Query("SELECT COUNT(fr) FROM FeeRecord fr WHERE fr.isPaid = 0")
    long countUnpaidRecords();

    @Query("SELECT fr.feeType, COALESCE(SUM(fr.amount), 0) FROM FeeRecord fr " +
           "WHERE fr.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY fr.feeType")
    List<Object[]> sumAmountByFeeTypeAndDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT fr FROM FeeRecord fr WHERE fr.createTime BETWEEN :startTime AND :endTime")
    List<FeeRecord> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT fr FROM FeeRecord fr WHERE fr.isPaid = 0")
    List<FeeRecord> findUnpaidRecords();
}
