package pl.kamann.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.Set;

@Service
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
        if (appUserRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                //todo: specific exception please :)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());
        appUser.setRoles(Set.of(userRole));

        appUserRepository.save(appUser);
    }

    public void registerInstructor(RegisterRequest request) {
        if (appUserRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Assign INSTRUCTOR role
        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                //todo: specific exception please :)
                .orElseThrow(() -> new RuntimeException("Instructor role not found"));

        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());
        appUser.setRoles(Set.of(instructorRole));

        appUserRepository.save(appUser);
    }
}
