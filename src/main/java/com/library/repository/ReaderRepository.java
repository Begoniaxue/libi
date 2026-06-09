package com.library.repository;

import com.library.entity.Reader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    Reader findByCardNo(String cardNo);

    Reader findByPhone(String phone);

    Page<Reader> findByNameContainingOrCardNoContainingOrPhoneContaining(
            String name, String cardNo, String phone, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reader r WHERE r.status = 1")
    long countActiveReaders();
}
