package com.securescan.service;

import com.securescan.dto.CreateScanRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ScanResponse;
import com.securescan.dto.UpdateScanRequest;
import com.securescan.entity.Scan;
import com.securescan.entity.User;
import com.securescan.enums.ScanStatus;
import com.securescan.exception.BadRequestException;
import com.securescan.exception.InvalidScanStatusException;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.UserRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages scan RECORDS. Creating a scan only stores a PENDING record - nothing is scanned.
 */
@Service
@Transactional(readOnly = true)
public class ScanService {

    private static final Logger log = LoggerFactory.getLogger(ScanService.class);

    private final ScanRepository scanRepository;
    private final UserRepository userRepository;
    private final ScannedFileRepository scannedFileRepository;

    public ScanService(ScanRepository scanRepository, UserRepository userRepository,
                       ScannedFileRepository scannedFileRepository) {
        this.scanRepository = scanRepository;
        this.userRepository = userRepository;
        this.scannedFileRepository = scannedFileRepository;
    }

    @Transactional
    public ScanResponse create(CreateScanRequest request) {
        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId()).orElseThrow(() -> {
                log.warn("Scan creation refused: user {} not found", request.userId());
                return new ResourceNotFoundException("User not found with id " + request.userId());
            });
        }
        Scan scan = new Scan();
        scan.setScanType(request.scanType());
        scan.setStatus(ScanStatus.PENDING);   // always PENDING; completedAt stays null
        scan.setUser(user);
        Scan saved = scanRepository.save(scan);
        log.info("Scan record created with id {} (status PENDING)", saved.getId());
        return toResponse(saved);
    }

    public PageResponse<ScanResponse> list(ScanStatus status, Long userId, Pageable pageable) {
        Page<Scan> page;
        if (status != null && userId != null) {
            page = scanRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (status != null) {
            page = scanRepository.findByStatus(status, pageable);
        } else if (userId != null) {
            page = scanRepository.findByUserId(userId, pageable);
        } else {
            page = scanRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(ScanService::toResponse));
    }

    public ScanResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Updates scanType (only while PENDING) and/or status (only along allowed transitions:
     * PENDING -> RUNNING, RUNNING -> COMPLETED, RUNNING -> FAILED).
     * This is a manual status update - nothing is actually scanned.
     */
    @Transactional
    public ScanResponse update(Long id, UpdateScanRequest request) {
        if (request.scanType() == null && request.status() == null) {
            throw new BadRequestException("Provide at least one of scanType or status");
        }
        Scan scan = findOrThrow(id);

        if (request.scanType() != null && request.scanType() != scan.getScanType()) {
            if (scan.getStatus() != ScanStatus.PENDING) {
                throw new ResourceConflictException(
                        "scanType can only be changed while the scan is PENDING (current: " + scan.getStatus() + ")");
            }
            scan.setScanType(request.scanType());
        }

        if (request.status() != null && request.status() != scan.getStatus()) {
            ScanStatus current = scan.getStatus();
            ScanStatus next = request.status();
            if (!current.canTransitionTo(next)) {
                log.warn("Scan {}: illegal status change {} -> {}", id, current, next);
                throw new InvalidScanStatusException(
                        "Invalid status transition: " + current + " -> " + next
                                + ". Allowed: PENDING->RUNNING, RUNNING->COMPLETED, RUNNING->FAILED");
            }
            scan.setStatus(next);
            if (next == ScanStatus.COMPLETED) {
                scan.setCompletedAt(LocalDateTime.now());
            }
        }
        Scan saved = scanRepository.save(scan);
        log.info("Scan record {} updated (status {})", id, saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Scan scan = findOrThrow(id);
        if (scannedFileRepository.existsByScanId(id)) {
            throw new ResourceConflictException(
                    "Scan " + id + " still has scanned-file records. Delete those files first.");
        }
        scanRepository.delete(scan);
        log.info("Scan record {} deleted", id);
    }

    private Scan findOrThrow(Long id) {
        return scanRepository.findById(id).orElseThrow(() -> {
            log.warn("Scan {} not found", id);
            return new ResourceNotFoundException("Scan not found with id " + id);
        });
    }

    static ScanResponse toResponse(Scan s) {
        Long userId = s.getUser() != null ? s.getUser().getId() : null;
        return new ScanResponse(s.getId(), s.getScanType(), s.getStatus(), userId,
                s.getCreatedAt(), s.getCompletedAt());
    }
}
