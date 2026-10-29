package com.securescan.entity;

import com.securescan.enums.Severity;
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

/** A threat RECORD supplied by a client. The application never creates these automatically. */
@Entity
@Table(name = "threats", indexes = {
        @Index(name = "idx_threats_file_id", columnList = "scanned_file_id"),
        @Index(name = "idx_threats_severity", columnList = "severity")
})
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "threat_name", nullable = false, length = 150)
    private String threatName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "detection_source", nullable = false, length = 100)
    private String detectionSource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scanned_file_id", nullable = false, foreignKey = @ForeignKey(name = "fk_threats_file"))
    private ScannedFile scannedFile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Threat() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getThreatName() { return threatName; }
    public void setThreatName(String threatName) { this.threatName = threatName; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getDetectionSource() { return detectionSource; }
    public void setDetectionSource(String detectionSource) { this.detectionSource = detectionSource; }
    public ScannedFile getScannedFile() { return scannedFile; }
    public void setScannedFile(ScannedFile scannedFile) { this.scannedFile = scannedFile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
