package com.securescan.dto;

import com.securescan.enums.QuarantineStatus;
import java.time.LocalDateTime;

public record QuarantineResponse(Long id, Long fileId, QuarantineStatus status, String reason,
                                 LocalDateTime createdAt) {
}
