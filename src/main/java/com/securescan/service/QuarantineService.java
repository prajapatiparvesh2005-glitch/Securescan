package com.securescan.service;

import com.securescan.dto.CreateQuarantineRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.QuarantineResponse;
import com.securescan.dto.UpdateQuarantineRequest;
import com.securescan.entity.QuarantineRecord;
import com.securescan.entity.ScannedFile;
import com.securescan.enums.QuarantineStatus;
import com.securescan.exception.BadRequestException;
import com.securescan.exception.InvalidScanStatusException;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.QuarantineRecordRepository;
import com.securescan.repository.ScannedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quarantine RECORDS only. No real file is isolated, moved, restored or deleted. */
@Service
@Transactional(readOnly = true)
public class QuarantineService {

    private static final Logger log = LoggerFactory.getLogger(QuarantineService.class);

    private final QuarantineRecordRepository quarantineRepository;
    private final ScannedFileRepository fileRepository;

    public QuarantineService(QuarantineRecordRepository quarantineRepository, ScannedFileRepository fileRepository) {
        this.quarantineRepository = quarantineRepository;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public QuarantineResponse create(CreateQuarantineRequest request) {
        ScannedFile file = fileRepository.findById(request.fileId()).orElseThrow(() -> {
            log.warn("Quarantine record refused: file {} not found", request.fileId());
            return new ResourceNotFoundException("Scanned file not found with id " + request.fileId());
        });
        if (quarantineRepository.existsByScannedFileIdAndStatus(file.getId(), QuarantineStatus.QUARANTINED)) {
            throw new ResourceConflictException(
                    "File " + file.getId() + " already has an active QUARANTINED record");
        }
        QuarantineRecord record = new QuarantineRecord();
        record.setScannedFile(file);
        record.setStatus(QuarantineStatus.QUARANTINED);
        record.setReason(request.reason().trim());
        QuarantineRecord saved = quarantineRepository.save(record);
        log.info("Quarantine record created with id {} for file {}", saved.getId(), file.getId());
        return toResponse(saved);
    }

    public PageResponse<QuarantineResponse> list(QuarantineStatus status, Long fileId, Pageable pageable) {
        Page<QuarantineRecord> page;
        if (status != null && fileId != null) {
            page = quarantineRepository.findByScannedFileIdAndStatus(fileId, status, pageable);
        } else if (status != null) {
            page = quarantineRepository.findByStatus(status, pageable);
        } else if (fileId != null) {
            page = quarantineRepository.findByScannedFileId(fileId, pageable);
        } else {
            page = quarantineRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(QuarantineService::toResponse));
    }

    public QuarantineResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    /** Allowed: QUARANTINED -> RESTORED, QUARANTINED -> RELEASED. RESTORED/RELEASED are final. */
    @Transactional
    public QuarantineResponse update(Long id, UpdateQuarantineRequest request) {
        QuarantineRecord record = findOrThrow(id);
        if (request.status() != record.getStatus()) {
            if (!record.getStatus().canTransitionTo(request.status())) {
                log.warn("Quarantine record {}: illegal status change {} -> {}", id, record.getStatus(), request.status());
                throw new InvalidScanStatusException("Invalid quarantine status transition: "
                        + record.getStatus() + " -> " + request.status()
                        + ". Allowed: QUARANTINED->RESTORED, QUARANTINED->RELEASED");
            }
            record.setStatus(request.status());
        }
        if (request.reason() != null) {
            if (request.reason().isBlank()) {
                throw new BadRequestException("reason must not be blank when supplied");
            }
            record.setReason(request.reason().trim());
        }
        QuarantineRecord saved = quarantineRepository.save(record);
        log.info("Quarantine record {} updated (status {})", id, saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        quarantineRepository.delete(findOrThrow(id));
        log.info("Quarantine record {} deleted", id);
    }

    private QuarantineRecord findOrThrow(Long id) {
        return quarantineRepository.findById(id).orElseThrow(() -> {
            log.warn("Quarantine record {} not found", id);
            return new ResourceNotFoundException("Quarantine record not found with id " + id);
        });
    }

    static QuarantineResponse toResponse(QuarantineRecord q) {
        return new QuarantineResponse(q.getId(), q.getScannedFile().getId(), q.getStatus(),
                q.getReason(), q.getCreatedAt());
    }
}
