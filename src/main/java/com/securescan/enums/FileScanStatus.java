package com.securescan.enums;

/** Status of one file record. SCANNED does NOT mean "clean" - it only means the record was marked as processed. */
public enum FileScanStatus { PENDING, SCANNED, ERROR }
