package com.securescan.repository;

import com.securescan.entity.Scan;
import com.securescan.enums.ScanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    Page<Scan> findByUserId(Long userId, Pageable pageable);

    Page<Scan> findByStatus(ScanStatus status, Pageable pageable);

    Page<Scan> findByUserIdAndStatus(Long userId, ScanStatus status, Pageable pageable);

    boolean existsByUserId(Long userId);
}
