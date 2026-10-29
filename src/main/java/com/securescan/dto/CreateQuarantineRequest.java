package com.securescan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateQuarantineRequest(
        @NotNull(message = "fileId is required")
        @Positive(message = "fileId must be a positive number")
        Long fileId,

        @NotBlank(message = "reason must not be blank")
        @Size(max = 500, message = "reason must be at most 500 characters")
        String reason) {
}
