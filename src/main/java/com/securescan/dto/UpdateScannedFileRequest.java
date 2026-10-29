package com.securescan.dto;

import com.securescan.enums.FileScanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Full replacement of the editable metadata. sha256Hash may be null (clears it);
 * scanStatus null means "leave unchanged". The owning scan cannot be changed.
 */
public record UpdateScannedFileRequest(
        @NotBlank(message = "fileName must not be blank")
        @Size(max = 255, message = "fileName must be at most 255 characters")
        String fileName,

        @NotBlank(message = "filePath must not be blank")
        @Size(max = 1024, message = "filePath must be at most 1024 characters")
        String filePath,

        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "sha256Hash must be exactly 64 hexadecimal characters")
        String sha256Hash,

        FileScanStatus scanStatus) {
}
