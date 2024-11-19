package pl.kamann.config.startup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.config.exception.specific.RoleNotFoundException;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.Set;

@Component
public class StartupSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StartupSeeder(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();

        if (appUserRepository.findByEmail("admin@admin.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RoleNotFoundException("ADMIN role not found in the system"));

            AppUser adminUser = new AppUser();
            adminUser.setEmail("admin@admin.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Adminek");
            adminUser.setRoles(Set.of(adminRole));

            appUserRepository.save(adminUser);
        }
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ADMIN"));
            roleRepository.save(new Role("INSTRUCTOR"));
            roleRepository.save(new Role("USER"));
        }
    }
}