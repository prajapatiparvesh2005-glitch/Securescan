package com.securescan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securescan.dto.CreateQuarantineRequest;
import com.securescan.dto.QuarantineResponse;
import com.securescan.dto.UpdateQuarantineRequest;
import com.securescan.entity.QuarantineRecord;
import com.securescan.entity.ScannedFile;
import com.securescan.enums.QuarantineStatus;
import com.securescan.exception.InvalidScanStatusException;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.QuarantineRecordRepository;
import com.securescan.repository.ScannedFileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuarantineServiceTest {

    @Mock private QuarantineRecordRepository quarantineRepository;
    @Mock private ScannedFileRepository fileRepository;
    @InjectMocks private QuarantineService quarantineService;

    private ScannedFile file(long id) {
        ScannedFile f = new ScannedFile();
        f.setId(id);
        return f;
    }

    private QuarantineRecord record(QuarantineStatus status) {
        QuarantineRecord r = new QuarantineRecord();
        r.setId(1L);
        r.setScannedFile(file(2L));
        r.setStatus(status);
        r.setReason("test reason");
        return r;
    }

    @Test
    void create_startsAsQuarantined() {
        when(fileRepository.findById(2L)).thenReturn(Optional.of(file(2L)));
        when(quarantineRepository.existsByScannedFileIdAndStatus(2L, QuarantineStatus.QUARANTINED)).thenReturn(false);
        when(quarantineRepository.save(any(QuarantineRecord.class))).thenAnswer(inv -> {
            QuarantineRecord r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        QuarantineResponse response = quarantineService.create(new CreateQuarantineRequest(2L, "Suspicious file"));

        assertEquals(QuarantineStatus.QUARANTINED, response.status());
        assertEquals(2L, response.fileId());
    }

    @Test
    void create_withUnknownFile_isNotFound() {
        when(fileRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> quarantineService.create(new CreateQuarantineRequest(2L, "reason")));
        verify(quarantineRepository, never()).save(any());
    }

    @Test
    void create_whenActiveRecordExists_isConflict() {
        when(fileRepository.findById(2L)).thenReturn(Optional.of(file(2L)));
        when(quarantineRepository.existsByScannedFileIdAndStatus(2L, QuarantineStatus.QUARANTINED)).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> quarantineService.create(new CreateQuarantineRequest(2L, "reason")));
    }

    @Test
    void update_quarantinedToReleased_isAllowed() {
        when(quarantineRepository.findById(1L)).thenReturn(Optional.of(record(QuarantineStatus.QUARANTINED)));
        when(quarantineRepository.save(any(QuarantineRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        QuarantineResponse response = quarantineService.update(1L, new UpdateQuarantineRequest(QuarantineStatus.RELEASED, null));

        assertEquals(QuarantineStatus.RELEASED, response.status());
    }

    @Test
    void update_releasedToQuarantined_isRejected() {
        when(quarantineRepository.findById(1L)).thenReturn(Optional.of(record(QuarantineStatus.RELEASED)));

        assertThrows(InvalidScanStatusException.class,
                () -> quarantineService.update(1L, new UpdateQuarantineRequest(QuarantineStatus.QUARANTINED, null)));
    }

    @Test
    void update_restoredToReleased_isRejected() {
        when(quarantineRepository.findById(1L)).thenReturn(Optional.of(record(QuarantineStatus.RESTORED)));

        assertThrows(InvalidScanStatusException.class,
                () -> quarantineService.update(1L, new UpdateQuarantineRequest(QuarantineStatus.RELEASED, null)));
    }
}
