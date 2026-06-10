package com.library.repository;

import com.library.entity.RenewLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RenewLogRepository extends JpaRepository<RenewLog, Long> {

    List<RenewLog> findByBorrowRecordIdOrderByCreateTimeDesc(Long borrowRecordId);

    Page<RenewLog> findByReaderIdOrderByCreateTimeDesc(Long readerId, Pageable pageable);

    List<RenewLog> findTop10ByReaderIdOrderByCreateTimeDesc(Long readerId);
}
