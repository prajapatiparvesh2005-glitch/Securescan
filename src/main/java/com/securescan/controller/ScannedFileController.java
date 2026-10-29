package com.securescan.controller;

import com.securescan.dto.ApiResponse;
import com.securescan.dto.CreateScannedFileRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ScannedFileResponse;
import com.securescan.dto.UpdateScannedFileRequest;
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
@RequestMapping("/api/files")
@Validated
@Tag(name = "Scanned Files", description = "File METADATA only. Files on disk are never read, executed, moved or deleted.")
public class ScannedFileController {

    private final ScannedFileService fileService;

    public ScannedFileController(ScannedFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    @Operation(summary = "Create scanned-file metadata for an existing scan",
            description = "404 if scanId does not exist; 409 if the scan is already COMPLETED or FAILED; 400 for invalid input.")
    public ResponseEntity<ApiResponse<ScannedFileResponse>> create(@Valid @RequestBody CreateScannedFileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Scanned-file record created successfully", fileService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List scanned-file metadata (paginated, optional filter scanId)")
    public ResponseEntity<ApiResponse<PageResponse<ScannedFileResponse>>> list(
            @RequestParam(required = false) @Positive(message = "must be greater than 0") Long scanId,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Scanned files retrieved successfully", fileService.list(scanId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scanned-file metadata by id")
    public ResponseEntity<ApiResponse<ScannedFileResponse>> get(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Scanned file retrieved successfully", fileService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update file metadata", description = "Replaces fileName, filePath, sha256Hash; scanStatus is optional.")
    public ResponseEntity<ApiResponse<ScannedFileResponse>> update(@PathVariable @Positive(message = "must be greater than 0") Long id,
                                                                   @Valid @RequestBody UpdateScannedFileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Scanned-file record updated successfully", fileService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file metadata", description = "204 on success; 409 if threat or quarantine records still reference it.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
