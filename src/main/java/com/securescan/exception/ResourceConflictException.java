package com.securescan.exception;

/** Duplicate data, or an operation blocked by related records (HTTP 409). */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
