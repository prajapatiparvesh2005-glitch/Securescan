package com.securescan.exception;

/** An illegal status transition (HTTP 409). */
public class InvalidScanStatusException extends RuntimeException {
    public InvalidScanStatusException(String message) {
        super(message);
    }
}
