package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.AppUserResponseDto;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.services.AppUserService;
import pl.kamann.services.AuthService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AppUserService appUserService;
    private final AuthService authService;


    @GetMapping
    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieve a paginated list of all users in the system filtered by role."
    )
    public ResponseEntity<PaginatedResponseDto<AppUserDto>> getAllUsersByRole(
            @ParameterObject  Pageable pageable,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(appUserService.getUsers(pageable, role));
    }

    @GetMapping("/logged")
    @Operation(
            summary = "Get details of logged in user.",
            description = "Retrieve an AppUserDto of currently logged in AppUser."
    )
    public ResponseEntity<AppUserResponseDto> getLoggedInUser(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getLoggedInAppUser(request));
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
