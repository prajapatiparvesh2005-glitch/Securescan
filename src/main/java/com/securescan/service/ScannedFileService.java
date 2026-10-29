package com.securescan.service;

import com.securescan.dto.CreateScannedFileRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ScannedFileResponse;
import com.securescan.dto.UpdateScannedFileRequest;
import com.securescan.entity.Scan;
import com.securescan.entity.ScannedFile;
import com.securescan.enums.FileScanStatus;
import com.securescan.enums.ScanStatus;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.QuarantineRecordRepository;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.ThreatRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Metadata only: file paths and hashes are stored as text and never read from disk. */
@Service
@Transactional(readOnly = true)
public class ScannedFileService {

    private static final Logger log = LoggerFactory.getLogger(ScannedFileService.class);

    private final ScannedFileRepository fileRepository;
    private final ScanRepository scanRepository;
    private final ThreatRepository threatRepository;
    private final QuarantineRecordRepository quarantineRepository;

    public ScannedFileService(ScannedFileRepository fileRepository, ScanRepository scanRepository,
                              ThreatRepository threatRepository, QuarantineRecordRepository quarantineRepository) {
        this.fileRepository = fileRepository;
        this.scanRepository = scanRepository;
        this.threatRepository = threatRepository;
        this.quarantineRepository = quarantineRepository;
    }

    @Transactional
    public ScannedFileResponse create(CreateScannedFileRequest request) {
        Scan scan = scanRepository.findById(request.scanId()).orElseThrow(() -> {
            log.warn("File creation refused: scan {} not found", request.scanId());
            return new ResourceNotFoundException("Scan not found with id " + request.scanId());
        });
        if (scan.getStatus() == ScanStatus.COMPLETED || scan.getStatus() == ScanStatus.FAILED) {
            throw new ResourceConflictException(
                    "Cannot add files to a scan that is already " + scan.getStatus());
        }
        ScannedFile file = new ScannedFile();
        file.setScan(scan);
        file.setFileName(request.fileName().trim());
        file.setFilePath(request.filePath().trim());
        file.setSha256Hash(normalizeHash(request.sha256Hash()));
        file.setScanStatus(FileScanStatus.PENDING);
        ScannedFile saved = fileRepository.save(file);
        log.info("Scanned-file record created with id {} for scan {}", saved.getId(), scan.getId());
        return toResponse(saved);
    }

    public PageResponse<ScannedFileResponse> list(Long scanId, Pageable pageable) {
        if (scanId != null) {
            return listByScan(scanId, pageable);
        }
        return PageResponse.from(fileRepository.findAll(pageable).map(ScannedFileService::toResponse));
    }

    /** Used by GET /api/scans/{id}/files - returns 404 when the scan itself does not exist. */
    public PageResponse<ScannedFileResponse> listByScan(Long scanId, Pageable pageable) {
        if (!scanRepository.existsById(scanId)) {
            log.warn("Scan {} not found", scanId);
            throw new ResourceNotFoundException("Scan not found with id " + scanId);
        }
        return PageResponse.from(fileRepository.findByScanId(scanId, pageable).map(ScannedFileService::toResponse));
    }

    public ScannedFileResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ScannedFileResponse update(Long id, UpdateScannedFileRequest request) {
        ScannedFile file = findOrThrow(id);
        file.setFileName(request.fileName().trim());
        file.setFilePath(request.filePath().trim());
        file.setSha256Hash(normalizeHash(request.sha256Hash()));
        if (request.scanStatus() != null) {
            file.setScanStatus(request.scanStatus());
        }
        ScannedFile saved = fileRepository.save(file);
        log.info("Scanned-file record {} updated", id);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        ScannedFile file = findOrThrow(id);
        if (threatRepository.existsByScannedFileId(id)) {
            throw new ResourceConflictException("File " + id + " still has threat records. Delete those first.");
        }
        if (quarantineRepository.existsByScannedFileId(id)) {
            throw new ResourceConflictException("File " + id + " still has quarantine records. Delete those first.");
        }
        fileRepository.delete(file);
        log.info("Scanned-file record {} deleted (metadata only; no real file touched)", id);
    }

    private ScannedFile findOrThrow(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> {
            log.warn("Scanned file {} not found", id);
            return new ResourceNotFoundException("Scanned file not found with id " + id);
        });
    }

    private static String normalizeHash(String hash) {
        return hash == null ? null : hash.toLowerCase(Locale.ROOT);
    }

    static ScannedFileResponse toResponse(ScannedFile f) {
        return new ScannedFileResponse(f.getId(), f.getScan().getId(), f.getFileName(), f.getFilePath(),
                f.getSha256Hash(), f.getScanStatus(), f.getCreatedAt());
    }
}
