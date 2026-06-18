package com.library.repository;

import com.library.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByNameContaining(String name, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.status = 1 AND a.startTime > :now ORDER BY a.startTime ASC")
    List<Activity> findUpcomingActivities(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.status = 1 ORDER BY a.createTime DESC")
    List<Activity> findPublishedActivities(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.status = 1")
    long countPublishedActivities();

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.status = 1 AND a.startTime > :now")
    long countUpcomingActivities(@Param("now") LocalDateTime now);
}
