package com.securescan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securescan.dto.CreateThreatRequest;
import com.securescan.dto.ThreatResponse;
import com.securescan.entity.ScannedFile;
import com.securescan.entity.Threat;
import com.securescan.enums.Severity;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScannedFileRepository;
import com.securescan.repository.ThreatRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThreatServiceTest {

    @Mock private ThreatRepository threatRepository;
    @Mock private ScannedFileRepository fileRepository;
    @InjectMocks private ThreatService threatService;

    @Test
    void create_storesSuppliedRecord() {
        ScannedFile file = new ScannedFile();
        file.setId(4L);
        when(fileRepository.findById(4L)).thenReturn(Optional.of(file));
        when(threatRepository.save(any(Threat.class))).thenAnswer(inv -> {
            Threat t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        ThreatResponse response = threatService.create(
                new CreateThreatRequest(4L, "Sample.Test.Threat", Severity.HIGH, "MANUAL_ENTRY"));

        assertEquals(4L, response.fileId());
        assertEquals(Severity.HIGH, response.severity());
        assertEquals("MANUAL_ENTRY", response.detectionSource());
    }

    @Test
    void create_withUnknownFile_isNotFound() {
        when(fileRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> threatService.create(new CreateThreatRequest(404L, "X", Severity.LOW, "MANUAL_ENTRY")));
        verify(threatRepository, never()).save(any());
    }

    @Test
    void get_missingThreat_isNotFound() {
        when(threatRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> threatService.get(8L));
    }
}
