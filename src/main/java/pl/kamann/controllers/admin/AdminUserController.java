package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.services.AppUserService;
import pl.kamann.services.AuthService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AppUserService appUserService;
    private final AuthService authService;

    @GetMapping
    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieve a paginated list of all users in the system sorted by role."
    )
    public ResponseEntity<PaginatedResponseDto<AppUserDto>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponseDto<AppUserDto> paginatedUsers = appUserService.getAllUsers(page, size);
        return ResponseEntity.ok(paginatedUsers);
    }

    @GetMapping("/by-role")
    @Operation(
            summary = "Get users by role with pagination",
            description = "Retrieve a paginated list of users filtered by role. Supported roles are CLIENT and INSTRUCTOR.")
    public ResponseEntity<Page<AppUserDto>> getUsersByRole(
            @RequestParam String role,
            @PageableDefault(size = 20) Pageable pageable // Ensure Pageable defaults
    ) {
        Page<AppUserDto> users = appUserService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Register a new user (client, instructor, or admin) with specified details and roles. The roles and other information are provided in the request body."
    )
    public ResponseEntity<AppUser> registerUser(@RequestBody RegisterRequest request) {
        AppUser registeredUser = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PutMapping("/activate/{userId}")
    @Operation(
            summary = "Activate a user account",
            description = "Activate a user account by setting its status to ACTIVE. This endpoint requires the user's ID."
    )
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        appUserService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/deactivate/{userId}")
    @Operation(
            summary = "Deactivate a user account",
            description = "Deactivate a user account by setting its status to INACTIVE. This endpoint requires the user's ID."
    )
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        appUserService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/status")
    @Operation(
            summary = "Change the status of a user",
            description = "Change the status of a user to ACTIVE, INACTIVE, or any other supported status. The new status is provided as a query parameter."
    )
    public ResponseEntity<Void> changeStatus(@PathVariable Long userId, @RequestParam AppUserStatus status) {
        appUserService.changeUserStatus(userId, status);
        return ResponseEntity.noContent().build();
    }
}
