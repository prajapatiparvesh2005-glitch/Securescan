package com.securescan.repository;

import com.securescan.entity.ScannedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long> {

    Page<ScannedFile> findByScanId(Long scanId, Pageable pageable);

    boolean existsByScanId(Long scanId);
}
