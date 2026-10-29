package com.securescan.controller;

import com.securescan.dto.ApiResponse;
import com.securescan.dto.CreateUserRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.UpdateUserRequest;
import com.securescan.dto.UserResponse;
import com.securescan.service.UserService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Users", description = "User records (no passwords, no login - authentication is out of scope)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a user record",
            description = "Returns 201 on success, 400 for validation errors, 409 for a duplicate username or email.")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User record created successfully", userService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List users (paginated)", description = "Query params: page (0-based), size (max 100), sort=field,asc|desc")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", userService.list(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id", description = "404 if the user does not exist.")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User retrieved successfully", userService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user's username and email", description = "404 if missing, 409 on duplicates.")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable @Positive(message = "must be greater than 0") Long id,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User record updated successfully", userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "204 on success, 404 if missing, 409 if the user still has scans.")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "must be greater than 0") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
