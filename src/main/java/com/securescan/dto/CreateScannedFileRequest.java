package com.securescan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateScannedFileRequest(
        @NotNull(message = "scanId is required")
        @Positive(message = "scanId must be a positive number")
        Long scanId,

        @NotBlank(message = "fileName must not be blank")
        @Size(max = 255, message = "fileName must be at most 255 characters")
        String fileName,

        @NotBlank(message = "filePath must not be blank")
        @Size(max = 1024, message = "filePath must be at most 1024 characters")
        String filePath,

        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "sha256Hash must be exactly 64 hexadecimal characters")
        String sha256Hash) {
}
