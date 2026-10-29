package com.securescan.controller;

import com.securescan.dto.ApiResponse;
import com.securescan.dto.CreateScanRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ScanResponse;
import com.securescan.dto.ScannedFileResponse;
import com.securescan.dto.UpdateScanRequest;
import com.securescan.enums.ScanStatus;
import com.securescan.service.ScanService;
import com.securescan.service.ScannedFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scans")
@Validated
@Tag(name = "Scans", description = "Scan RECORDS. Creating one stores a PENDING record; nothing is actually scanned.")
public class ScanController {

    private final ScanService scanService;
    private final ScannedFileService fileService;

    public ScanController(ScanService scanService, ScannedFileService fileService) {
        this.scanService = scanService;
        this.fileService = fileService;
    }

    @PostMapping
    @Operation(summary = "Create a scan record (status PENDING)",
            description = "userId is optional. 404 if userId does not exist, 400 for invalid input.")
    public ResponseEntity<ApiResponse<ScanResponse>> create(@Valid @RequestBody CreateScanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Scan record created successfully", scanService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List scan records (paginated, optional filters status and userId)")
    public ResponseEntity<ApiResponse<PageResponse<ScanResponse>>> list(
            @RequestParam(required = false) ScanStatus status,
            @RequestParam(required = false) @Positive(message = "must be greater than 0") Long userId,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Scans retrieved successfully", scanService.list(status, userId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a scan record by id")
    public ResponseEntity<ApiResponse<ScanResponse>> get(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Scan retrieved successfully", scanService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update scanType and/or status of a scan record",
            description = "Manual status update (nothing is actually scanned). Allowed transitions: PENDING->RUNNING, "
                    + "RUNNING->COMPLETED, RUNNING->FAILED. completedAt is set only when moving to COMPLETED. "
                    + "scanType can only change while PENDING. Illegal transitions return 409.")
    public ResponseEntity<ApiResponse<ScanResponse>> update(@PathVariable @Positive(message = "must be greater than 0") Long id,
                                                            @RequestBody UpdateScanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Scan record updated successfully", scanService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scan record", description = "204 on success, 404 if missing, 409 if it still has file records.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        scanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/files")
    @Operation(summary = "List scanned-file metadata of one scan (paginated)", description = "404 if the scan does not exist.")
    public ResponseEntity<ApiResponse<PageResponse<ScannedFileResponse>>> files(
            @PathVariable @Positive(message = "must be greater than 0") Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Scanned files retrieved successfully", fileService.listByScan(id, pageable)));
    }
}
