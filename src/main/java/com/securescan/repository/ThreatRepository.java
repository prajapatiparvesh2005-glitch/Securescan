package com.securescan.repository;

import com.securescan.entity.Threat;
import com.securescan.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreatRepository extends JpaRepository<Threat, Long> {

    Page<Threat> findBySeverity(Severity severity, Pageable pageable);

    Page<Threat> findByScannedFileId(Long fileId, Pageable pageable);

    Page<Threat> findByScannedFileIdAndSeverity(Long fileId, Severity severity, Pageable pageable);

    boolean existsByScannedFileId(Long fileId);
}
