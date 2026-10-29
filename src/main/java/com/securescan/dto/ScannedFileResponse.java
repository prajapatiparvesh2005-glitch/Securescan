package com.securescan.dto;

import com.securescan.enums.FileScanStatus;
import java.time.LocalDateTime;

public record ScannedFileResponse(Long id, Long scanId, String fileName, String filePath, String sha256Hash,
                                  FileScanStatus scanStatus, LocalDateTime createdAt) {
}
