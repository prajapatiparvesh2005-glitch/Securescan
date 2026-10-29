package com.securescan.controller;

import com.securescan.dto.ApiResponse;
import com.securescan.dto.CreateQuarantineRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.QuarantineResponse;
import com.securescan.dto.UpdateQuarantineRequest;
import com.securescan.enums.QuarantineStatus;
import com.securescan.service.QuarantineService;
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
@RequestMapping("/api/quarantine")
@Validated
@Tag(name = "Quarantine", description = "Quarantine RECORDS in the database only. No real file is isolated, moved or deleted.")
public class QuarantineController {

    private final QuarantineService quarantineService;

    public QuarantineController(QuarantineService quarantineService) {
        this.quarantineService = quarantineService;
    }

    @PostMapping
    @Operation(summary = "Create a quarantine record (status QUARANTINED) for an existing file",
            description = "Database record only. 404 if fileId is unknown; 409 if the file already has an active QUARANTINED record.")
    public ResponseEntity<ApiResponse<QuarantineResponse>> create(@Valid @RequestBody CreateQuarantineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Quarantine record created successfully", quarantineService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List quarantine records (paginated, optional filters status and fileId)")
    public ResponseEntity<ApiResponse<PageResponse<QuarantineResponse>>> list(
            @RequestParam(required = false) QuarantineStatus status,
            @RequestParam(required = false) @Positive(message = "must be greater than 0") Long fileId,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Quarantine records retrieved successfully",
                quarantineService.list(status, fileId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a quarantine record by id")
    public ResponseEntity<ApiResponse<QuarantineResponse>> get(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Quarantine record retrieved successfully", quarantineService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the status (and optionally reason) of a quarantine record",
            description = "Allowed transitions: QUARANTINED->RESTORED, QUARANTINED->RELEASED. Others return 409.")
    public ResponseEntity<ApiResponse<QuarantineResponse>> update(@PathVariable @Positive(message = "must be greater than 0") Long id,
                                                                  @Valid @RequestBody UpdateQuarantineRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Quarantine record updated successfully", quarantineService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a quarantine record")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        quarantineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
