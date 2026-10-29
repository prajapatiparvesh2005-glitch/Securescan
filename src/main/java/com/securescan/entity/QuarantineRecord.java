package com.securescan.entity;

import com.securescan.enums.QuarantineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** A quarantine RECORD only. No real file is isolated, moved or deleted. */
@Entity
@Table(name = "quarantine_records", indexes = {
        @Index(name = "idx_quarantine_file_id", columnList = "scanned_file_id"),
        @Index(name = "idx_quarantine_status", columnList = "status")
})
public class QuarantineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scanned_file_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quarantine_file"))
    private ScannedFile scannedFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuarantineStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public QuarantineRecord() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ScannedFile getScannedFile() { return scannedFile; }
    public void setScannedFile(ScannedFile scannedFile) { this.scannedFile = scannedFile; }
    public QuarantineStatus getStatus() { return status; }
    public void setStatus(QuarantineStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
