package com.securescan.enums;

public enum QuarantineStatus {
    QUARANTINED, RESTORED, RELEASED;

    /** Allowed transitions: QUARANTINED -> RESTORED, QUARANTINED -> RELEASED. RESTORED and RELEASED are final. */
    public boolean canTransitionTo(QuarantineStatus next) {
        return this == QUARANTINED && (next == RESTORED || next == RELEASED);
    }
}
