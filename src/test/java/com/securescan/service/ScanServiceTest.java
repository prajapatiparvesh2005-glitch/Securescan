package com.securescan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securescan.dto.CreateScanRequest;
import com.securescan.dto.ScanResponse;
import com.securescan.dto.UpdateScanRequest;
import com.securescan.entity.Scan;
import com.securescan.entity.User;
import com.securescan.enums.ScanStatus;
import com.securescan.enums.ScanType;
import com.securescan.exception.BadRequestException;
import com.securescan.exception.InvalidScanStatusException;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock private ScanRepository scanRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScannedFileRepository scannedFileRepository;
    @InjectMocks private ScanService scanService;

    private Scan scanWithStatus(ScanStatus status) {
        Scan scan = new Scan();
        scan.setId(1L);
        scan.setScanType(ScanType.FULL_SCAN);
        scan.setStatus(status);
        scan.setCreatedAt(LocalDateTime.now());
        return scan;
    }

    @Test
    void create_storesPendingRecord_withoutCompletedAt() {
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> {
            Scan s = inv.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        ScanResponse response = scanService.create(new CreateScanRequest(ScanType.QUICK_SCAN, null));

        assertEquals(ScanStatus.PENDING, response.status());
        assertEquals(ScanType.QUICK_SCAN, response.scanType());
        assertNull(response.completedAt());
        assertNull(response.userId());
    }

    @Test
    void create_withExistingUser_linksUser() {
        User user = new User("alice", "alice@example.com");
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> {
            Scan s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        ScanResponse response = scanService.create(new CreateScanRequest(ScanType.FULL_SCAN, 5L));

        assertEquals(5L, response.userId());
    }

    @Test
    void create_withUnknownUser_throwsNotFound_andSavesNothing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> scanService.create(new CreateScanRequest(ScanType.FULL_SCAN, 99L)));
        verify(scanRepository, never()).save(any());
    }

    @Test
    void get_missingScan_throwsNotFound() {
        when(scanRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> scanService.get(999L));
    }

    @Test
    void update_pendingToRunning_isAllowed() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.PENDING)));
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanResponse response = scanService.update(1L, new UpdateScanRequest(null, ScanStatus.RUNNING));

        assertEquals(ScanStatus.RUNNING, response.status());
        assertNull(response.completedAt());
    }

    @Test
    void update_pendingToCompleted_isRejected() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.PENDING)));

        assertThrows(InvalidScanStatusException.class,
                () -> scanService.update(1L, new UpdateScanRequest(null, ScanStatus.COMPLETED)));
        verify(scanRepository, never()).save(any());
    }

    @Test
    void update_runningToCompleted_setsCompletedAt() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.RUNNING)));
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanResponse response = scanService.update(1L, new UpdateScanRequest(null, ScanStatus.COMPLETED));

        assertEquals(ScanStatus.COMPLETED, response.status());
        assertNotNull(response.completedAt());
    }

    @Test
    void update_runningToFailed_leavesCompletedAtNull() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.RUNNING)));
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanResponse response = scanService.update(1L, new UpdateScanRequest(null, ScanStatus.FAILED));

        assertEquals(ScanStatus.FAILED, response.status());
        assertNull(response.completedAt());
    }

    @Test
    void update_completedToRunning_isRejected() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.COMPLETED)));

        assertThrows(InvalidScanStatusException.class,
                () -> scanService.update(1L, new UpdateScanRequest(null, ScanStatus.RUNNING)));
    }

    @Test
    void update_scanTypeChange_onlyWhilePending() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.RUNNING)));

        assertThrows(ResourceConflictException.class,
                () -> scanService.update(1L, new UpdateScanRequest(ScanType.QUICK_SCAN, null)));
    }

    @Test
    void update_missingScan_throwsNotFound() {
        when(scanRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> scanService.update(404L, new UpdateScanRequest(null, ScanStatus.RUNNING)));
    }

    @Test
    void update_emptyRequest_isBadRequest() {
        assertThrows(BadRequestException.class, () -> scanService.update(1L, new UpdateScanRequest(null, null)));
    }

    @Test
    void delete_scanWithFiles_isConflict() {
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scanWithStatus(ScanStatus.PENDING)));
        when(scannedFileRepository.existsByScanId(1L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> scanService.delete(1L));
        verify(scanRepository, never()).delete(any());
    }
}
