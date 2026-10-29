package com.securescan.service;

import com.securescan.dto.CreateUserRequest;
import com.securescan.dto.PageResponse;
import com.securescan.dto.UpdateUserRequest;
import com.securescan.dto.UserResponse;
import com.securescan.entity.User;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.UserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ScanRepository scanRepository;

    public UserService(UserRepository userRepository, ScanRepository scanRepository) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResourceConflictException("Username already exists: " + username);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email already exists: " + email);
        }
        User saved = userRepository.save(new User(username, email));
        log.info("User record created with id {}", saved.getId());
        return toResponse(saved);
    }

    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(UserService::toResponse));
    }

    public UserResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findOrThrow(id);
        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ResourceConflictException("Username already exists: " + username);
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResourceConflictException("Email already exists: " + email);
        }
        user.setUsername(username);
        user.setEmail(email);
        User saved = userRepository.save(user);
        log.info("User record {} updated", id);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = findOrThrow(id);
        if (scanRepository.existsByUserId(id)) {
            throw new ResourceConflictException(
                    "User " + id + " still has scan records. Delete those scans first.");
        }
        userRepository.delete(user);
        log.info("User record {} deleted", id);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User {} not found", id);
            return new ResourceNotFoundException("User not found with id " + id);
        });
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt());
    }
}
