package com.securescan.repository;

import com.securescan.entity.QuarantineRecord;
import com.securescan.enums.QuarantineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuarantineRecordRepository extends JpaRepository<QuarantineRecord, Long> {

    Page<QuarantineRecord> findByStatus(QuarantineStatus status, Pageable pageable);

    Page<QuarantineRecord> findByScannedFileId(Long fileId, Pageable pageable);

    Page<QuarantineRecord> findByScannedFileIdAndStatus(Long fileId, QuarantineStatus status, Pageable pageable);

    boolean existsByScannedFileId(Long fileId);

    boolean existsByScannedFileIdAndStatus(Long fileId, QuarantineStatus status);
}
