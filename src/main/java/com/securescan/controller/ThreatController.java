package com.securescan.controller;

import com.securescan.dto.ApiResponse;
import com.securescan.dto.CreateThreatRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.ThreatResponse;
import com.securescan.dto.UpdateThreatRequest;
import com.securescan.enums.Severity;
import com.securescan.service.ThreatService;
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
@RequestMapping("/api/threats")
@Validated
@Tag(name = "Threats", description = "Threat RECORDS supplied by clients. The API never detects or invents threats.")
public class ThreatController {

    private final ThreatService threatService;

    public ThreatController(ThreatService threatService) {
        this.threatService = threatService;
    }

    @PostMapping
    @Operation(summary = "Create a threat record for an existing file", description = "404 if fileId does not exist; 400 for invalid input.")
    public ResponseEntity<ApiResponse<ThreatResponse>> create(@Valid @RequestBody CreateThreatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Threat record created successfully", threatService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List threat records (paginated, optional filters severity and fileId)")
    public ResponseEntity<ApiResponse<PageResponse<ThreatResponse>>> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) @Positive(message = "must be greater than 0") Long fileId,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Threats retrieved successfully", threatService.list(severity, fileId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a threat record by id")
    public ResponseEntity<ApiResponse<ThreatResponse>> get(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Threat retrieved successfully", threatService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update threatName, severity and detectionSource")
    public ResponseEntity<ApiResponse<ThreatResponse>> update(@PathVariable @Positive(message = "must be greater than 0") Long id,
                                                              @Valid @RequestBody UpdateThreatRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Threat record updated successfully", threatService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a threat record")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        threatService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
