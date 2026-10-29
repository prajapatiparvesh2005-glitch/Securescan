package com.securescan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securescan.dto.CreateUserRequest;
import com.securescan.dto.UserResponse;
import com.securescan.entity.User;
import com.securescan.exception.ResourceConflictException;
import com.securescan.exception.ResourceNotFoundException;
import com.securescan.repository.ScanRepository;
import com.securescan.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ScanRepository scanRepository;
    @InjectMocks private UserService userService;

    @Test
    void create_normalizesEmail_andSaves() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(new CreateUserRequest("  alice ", "Alice@Example.COM "));

        assertEquals("alice", response.username());
        assertEquals("alice@example.com", response.email());
    }

    @Test
    void create_duplicateUsername_isConflict() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> userService.create(new CreateUserRequest("alice", "a@example.com")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_duplicateEmail_isConflict() {
        when(userRepository.existsByEmailIgnoreCase("a@example.com")).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> userService.create(new CreateUserRequest("alice", "a@example.com")));
    }

    @Test
    void get_missingUser_isNotFound() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.get(7L));
    }

    @Test
    void delete_userWithScans_isConflict() {
        User user = new User("alice", "alice@example.com");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(scanRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> userService.delete(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_userWithoutScans_deletes() {
        User user = new User("alice", "alice@example.com");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(scanRepository.existsByUserId(1L)).thenReturn(false);

        userService.delete(1L);

        verify(userRepository).delete(user);
    }
}
