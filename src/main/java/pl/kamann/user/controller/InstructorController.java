package pl.kamann.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class InstructorController {

    private final AppUserRepository appUserRepository;

    public InstructorController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<AppUser> getUserDetails(Principal principal) {
        String email = principal.getName();
        AppUser user = appUserRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
}