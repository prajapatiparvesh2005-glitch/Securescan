package com.securescan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.securescan.dto.ScanResponse;
import com.securescan.enums.ScanStatus;
import com.securescan.enums.ScanType;
import com.securescan.exception.InvalidScanStatusException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.service.ScanService;
import com.securescan.service.ScannedFileService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Web-layer test: real controller, validation and exception handler; mocked services (no database needed). */
@WebMvcTest(ScanController.class)
class ScanControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ScanService scanService;
    @MockBean private ScannedFileService scannedFileService;

    @Test
    void createScan_returns201_withPendingStatus() throws Exception {
        when(scanService.create(any())).thenReturn(
                new ScanResponse(1L, ScanType.FULL_SCAN, ScanStatus.PENDING, null, LocalDateTime.now(), null));

        mockMvc.perform(post("/api/scans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scanType\":\"FULL_SCAN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createScan_missingScanType_returns400_withFieldError() throws Exception {
        mockMvc.perform(post("/api/scans").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.scanType").exists());
    }

    @Test
    void createScan_invalidEnumValue_returns400() throws Exception {
        mockMvc.perform(post("/api/scans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scanType\":\"SUPER_SCAN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getScan_missing_returns404() throws Exception {
        when(scanService.get(999L)).thenThrow(new ResourceNotFoundException("Scan not found with id 999"));

        mockMvc.perform(get("/api/scans/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Scan not found with id 999"));
    }

    @Test
    void getScan_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/scans/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void getScan_nonPositiveId_returns400() throws Exception {
        mockMvc.perform(get("/api/scans/0")).andExpect(status().isBadRequest());
    }

    @Test
    void updateScan_invalidTransition_returns409() throws Exception {
        when(scanService.update(eq(1L), any())).thenThrow(
                new InvalidScanStatusException("Invalid status transition: PENDING -> COMPLETED"));

        mockMvc.perform(put("/api/scans/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteScan_returns204() throws Exception {
        mockMvc.perform(delete("/api/scans/1")).andExpect(status().isNoContent());
    }
}
