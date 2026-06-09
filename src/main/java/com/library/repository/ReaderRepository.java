package com.library.repository;

import com.library.entity.Reader;
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
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    Reader findByCardNo(String cardNo);

    Reader findByPhone(String phone);

    Page<Reader> findByNameContainingOrCardNoContainingOrPhoneContaining(
            String name, String cardNo, String phone, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reader r WHERE r.status = 1")
    long countActiveReaders();

    @Query("SELECT r.identityType, COUNT(r) FROM Reader r WHERE r.status = 1 GROUP BY r.identityType")
    List<Object[]> countByIdentityType();

    @Query("SELECT CASE " +
           "WHEN r.birthday IS NULL THEN '未知' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) < 18 THEN '18岁以下' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 18 AND 25 THEN '18-25岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 26 AND 35 THEN '26-35岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 36 AND 45 THEN '36-45岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 46 AND 55 THEN '46-55岁' " +
           "ELSE '55岁以上' END as ageGroup, COUNT(r) " +
           "FROM Reader r WHERE r.status = 1 GROUP BY ageGroup")
    List<Object[]> countByAgeGroup();

    @Query("SELECT CASE " +
           "WHEN r.creditScore >= 90 THEN '优秀(90-100)' " +
           "WHEN r.creditScore >= 80 THEN '良好(80-89)' " +
           "WHEN r.creditScore >= 70 THEN '一般(70-79)' " +
           "WHEN r.creditScore >= 60 THEN '及格(60-69)' " +
           "ELSE '不及格(<60)' END as creditLevel, COUNT(r) " +
           "FROM Reader r WHERE r.status = 1 GROUP BY creditLevel")
    List<Object[]> countByCreditLevel();

    @Query("SELECT COUNT(r) FROM Reader r WHERE r.status = 1 AND r.violationCount > 0")
    long countReadersWithViolations();

    @Query("SELECT r.identityType, b.category, COUNT(br) " +
           "FROM BorrowRecord br " +
           "JOIN Reader r ON br.readerId = r.id " +
           "JOIN Book b ON br.bookId = b.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.identityType, b.category")
    List<Object[]> findReaderPreferenceByIdentity(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE " +
           "WHEN r.birthday IS NULL THEN '未知' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) < 18 THEN '18岁以下' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 18 AND 25 THEN '18-25岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 26 AND 35 THEN '26-35岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 36 AND 45 THEN '36-45岁' " +
           "WHEN TIMESTAMPDIFF(YEAR, r.birthday, CURRENT_DATE) BETWEEN 46 AND 55 THEN '46-55岁' " +
           "ELSE '55岁以上' END as ageGroup, b.category, COUNT(br) " +
           "FROM BorrowRecord br " +
           "JOIN Reader r ON br.readerId = r.id " +
           "JOIN Book b ON br.bookId = b.id " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ageGroup, b.category")
    List<Object[]> findReaderPreferenceByAge(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM Reader r WHERE r.status = 1 AND r.createTime BETWEEN :startTime AND :endTime")
    long countByCreateTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
