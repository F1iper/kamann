package pl.kamann.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.exception.specific.EmailAlreadyExistsException;
import pl.kamann.exception.specific.RoleNotFoundException;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.Set;

@Service
@Slf4j
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerClient(RegisterRequest request) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Default USER role not found in the system"));
        registerUser(request, userRole);
    }

    public void registerInstructor(RegisterRequest request) {
        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new RoleNotFoundException("INSTRUCTOR role not found in the system"));
        registerUser(request, instructorRole);
    }

    private void registerUser(RegisterRequest request, Role role) {
        validateEmailNotTaken(request.email());

        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());
        appUser.setRoles(Set.of(role));

        appUserRepository.save(appUser);
        log.info("User registered successfully with email: {} and role: {}", request.email(), role.getName());
    }

    private void validateEmailNotTaken(String email) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            log.warn("Registration attempt with existing email: {}", email);
            throw new EmailAlreadyExistsException(email);
        }
    }
}