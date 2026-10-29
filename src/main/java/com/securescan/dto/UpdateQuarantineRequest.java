package com.securescan.dto;

import com.securescan.enums.QuarantineStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** status is required; reason is optional (null = leave unchanged). */
public record UpdateQuarantineRequest(
        @NotNull(message = "status is required (QUARANTINED, RESTORED or RELEASED)")
        QuarantineStatus status,

        @Size(max = 500, message = "reason must be at most 500 characters")
        String reason) {
}
