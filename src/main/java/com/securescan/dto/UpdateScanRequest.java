package com.securescan.dto;

import com.securescan.enums.ScanStatus;
import com.securescan.enums.ScanType;

/** Both fields are optional; null means "leave unchanged". At least one must be supplied. */
public record UpdateScanRequest(ScanType scanType, ScanStatus status) {
}
