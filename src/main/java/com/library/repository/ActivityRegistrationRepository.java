package com.library.repository;

import com.library.entity.ActivityRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

    Page<ActivityRegistration> findByActivityId(Long activityId, Pageable pageable);

    List<ActivityRegistration> findByActivityId(Long activityId);

    Page<ActivityRegistration> findByActivityIdAndNameContaining(Long activityId, String name, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ActivityRegistration r WHERE r.activityId = :activityId AND r.status = 1")
    long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT r FROM ActivityRegistration r WHERE r.activityId = :activityId AND r.phone = :phone AND r.status = 1")
    Optional<ActivityRegistration> findByActivityIdAndPhone(@Param("activityId") Long activityId, @Param("phone") String phone);

    @Query("SELECT r FROM ActivityRegistration r WHERE r.readerId = :readerId ORDER BY r.createTime DESC")
    List<ActivityRegistration> findByReaderId(@Param("readerId") Long readerId);

    @Query("SELECT COUNT(r) FROM ActivityRegistration r WHERE r.readerId = :readerId AND r.activityId = :activityId AND r.status = 1")
    long countByReaderIdAndActivityId(@Param("readerId") Long readerId, @Param("activityId") Long activityId);
}
