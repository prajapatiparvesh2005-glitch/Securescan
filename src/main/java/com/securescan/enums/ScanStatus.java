package com.securescan.enums;

public enum ScanStatus {
    PENDING, RUNNING, COMPLETED, FAILED;

    /** Allowed transitions: PENDING -> RUNNING, RUNNING -> COMPLETED, RUNNING -> FAILED. */
    public boolean canTransitionTo(ScanStatus next) {
        return switch (this) {
            case PENDING -> next == RUNNING;
            case RUNNING -> next == COMPLETED || next == FAILED;
            case COMPLETED, FAILED -> false;
        };
    }
}
