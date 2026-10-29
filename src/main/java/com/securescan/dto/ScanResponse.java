package com.securescan.dto;

import com.securescan.enums.ScanStatus;
import com.securescan.enums.ScanType;
import java.time.LocalDateTime;

public record ScanResponse(Long id, ScanType scanType, ScanStatus status, Long userId,
                           LocalDateTime createdAt, LocalDateTime completedAt) {
}
