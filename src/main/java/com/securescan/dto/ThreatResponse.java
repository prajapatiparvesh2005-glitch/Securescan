package com.securescan.dto;

import com.securescan.enums.Severity;
import java.time.LocalDateTime;

public record ThreatResponse(Long id, Long fileId, String threatName, Severity severity,
                             String detectionSource, LocalDateTime createdAt) {
}
