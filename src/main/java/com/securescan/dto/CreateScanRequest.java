package com.securescan.dto;

import com.securescan.enums.ScanType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateScanRequest(
        @NotNull(message = "scanType is required (QUICK_SCAN, FULL_SCAN or CUSTOM_SCAN)")
        ScanType scanType,

        @Positive(message = "userId must be a positive number")
        Long userId) {
}
