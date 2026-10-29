package com.securescan.service;

import com.securescan.dto.CreateThreatRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ThreatResponse;
import com.securescan.dto.UpdateThreatRequest;
import com.securescan.entity.ScannedFile;
import com.securescan.entity.Threat;
import com.securescan.enums.Severity;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.ThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stores threat RECORDS that a client explicitly submits. Nothing is detected or generated here. */
@Service
@Transactional(readOnly = true)
public class ThreatService {

    private static final Logger log = LoggerFactory.getLogger(ThreatService.class);

    private final ThreatRepository threatRepository;
    private final ScannedFileRepository fileRepository;

    public ThreatService(ThreatRepository threatRepository, ScannedFileRepository fileRepository) {
        this.threatRepository = threatRepository;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public ThreatResponse create(CreateThreatRequest request) {
        ScannedFile file = fileRepository.findById(request.fileId()).orElseThrow(() -> {
            log.warn("Threat creation refused: file {} not found", request.fileId());
            return new ResourceNotFoundException("Scanned file not found with id " + request.fileId());
        });
        Threat threat = new Threat();
        threat.setScannedFile(file);
        threat.setThreatName(request.threatName().trim());
        threat.setSeverity(request.severity());
        threat.setDetectionSource(request.detectionSource().trim());
        Threat saved = threatRepository.save(threat);
        log.info("Threat record created with id {} for file {}", saved.getId(), file.getId());
        return toResponse(saved);
    }

    public PageResponse<ThreatResponse> list(Severity severity, Long fileId, Pageable pageable) {
        Page<Threat> page;
        if (severity != null && fileId != null) {
            page = threatRepository.findByScannedFileIdAndSeverity(fileId, severity, pageable);
        } else if (severity != null) {
            page = threatRepository.findBySeverity(severity, pageable);
        } else if (fileId != null) {
            page = threatRepository.findByScannedFileId(fileId, pageable);
        } else {
            page = threatRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(ThreatService::toResponse));
    }

    public ThreatResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ThreatResponse update(Long id, UpdateThreatRequest request) {
        Threat threat = findOrThrow(id);
        threat.setThreatName(request.threatName().trim());
        threat.setSeverity(request.severity());
        threat.setDetectionSource(request.detectionSource().trim());
        Threat saved = threatRepository.save(threat);
        log.info("Threat record {} updated", id);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        threatRepository.delete(findOrThrow(id));
        log.info("Threat record {} deleted", id);
    }

    private Threat findOrThrow(Long id) {
        return threatRepository.findById(id).orElseThrow(() -> {
            log.warn("Threat {} not found", id);
            return new ResourceNotFoundException("Threat not found with id " + id);
        });
    }

    static ThreatResponse toResponse(Threat t) {
        return new ThreatResponse(t.getId(), t.getScannedFile().getId(), t.getThreatName(),
                t.getSeverity(), t.getDetectionSource(), t.getCreatedAt());
    }
}
