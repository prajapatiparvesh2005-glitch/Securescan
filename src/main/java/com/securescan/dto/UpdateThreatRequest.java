package com.securescan.dto;

import com.securescan.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateThreatRequest(
        @NotBlank(message = "threatName must not be blank")
        @Size(max = 150, message = "threatName must be at most 150 characters")
        String threatName,

        @NotNull(message = "severity is required (LOW, MEDIUM, HIGH or CRITICAL)")
        Severity severity,

        @NotBlank(message = "detectionSource must not be blank")
        @Size(max = 100, message = "detectionSource must be at most 100 characters")
        String detectionSource) {
}
