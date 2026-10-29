package com.securescan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securescan.dto.CreateScannedFileRequest;
import com.securescan.dto.ScannedFileResponse;
import com.securescan.entity.Scan;
import com.securescan.entity.ScannedFile;
import com.securescan.enums.FileScanStatus;
import com.securescan.enums.ScanStatus;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.QuarantineRecordRepository;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.ThreatRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScannedFileServiceTest {

    private static final String HASH_UPPER = "A".repeat(64);

    @Mock private ScannedFileRepository fileRepository;
    @Mock private ScanRepository scanRepository;
    @Mock private ThreatRepository threatRepository;
    @Mock private QuarantineRecordRepository quarantineRepository;
    @InjectMocks private ScannedFileService fileService;

    private Scan scan(ScanStatus status) {
        Scan scan = new Scan();
        scan.setId(1L);
        scan.setStatus(status);
        return scan;
    }

    @Test
    void create_storesMetadata_withPendingStatus_andLowercaseHash() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scan(ScanStatus.PENDING)));
        when(fileRepository.save(any(ScannedFile.class))).thenAnswer(inv -> {
            ScannedFile f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        ScannedFileResponse response = fileService.create(
                new CreateScannedFileRequest(1L, "report.docx", "C:/docs/report.docx", HASH_UPPER));

        assertEquals(FileScanStatus.PENDING, response.scanStatus());
        assertEquals("a".repeat(64), response.sha256Hash());
        assertEquals(1L, response.scanId());
    }

    @Test
    void create_withUnknownScan_isNotFound() {
        when(scanRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fileService.create(new CreateScannedFileRequest(5L, "a.txt", "/tmp/a.txt", null)));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void create_onCompletedScan_isConflict() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scan(ScanStatus.COMPLETED)));

        assertThrows(ResourceConflictException.class,
                () -> fileService.create(new CreateScannedFileRequest(1L, "a.txt", "/tmp/a.txt", null)));
    }

    @Test
    void listByScan_unknownScan_isNotFound() {
        when(scanRepository.existsById(9L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> fileService.listByScan(9L, org.springframework.data.domain.Pageable.unpaged()));
    }

    @Test
    void delete_fileWithThreats_isConflict() {
        ScannedFile file = new ScannedFile();
        file.setId(3L);
        when(fileRepository.findById(3L)).thenReturn(Optional.of(file));
        when(threatRepository.existsByScannedFileId(3L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> fileService.delete(3L));
        verify(fileRepository, never()).delete(any());
    }
}
