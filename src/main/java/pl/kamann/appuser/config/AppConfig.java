package pl.kamann.appuser.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.model.Role;
import pl.kamann.appuser.service.AppUserService;

@Configuration
public class AppConfig {

    private final AppUserService userService;

    @Autowired
    public AppConfig(AppUserService userService) {
        this.userService = userService;
    }

    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            if (userService.getAllAppUsers().isEmpty()) {
                AppUser admin = new AppUser();
                String adminPassword = "admin";
                admin.setRole(Role.ADMIN);
                admin.setFirstName("Firstname");
                admin.setLastName("Lastname");
                admin.setEmail("admin@admin.com");
                userService.saveUser(admin, adminPassword);

                AppUser instructor = new AppUser();
                String instructorPassword = "instructor";
                instructor.setRole(Role.INSTRUCTOR);
                instructor.setFirstName("Firstname");
                instructor.setLastName("Lastname");
                instructor.setEmail("instructor@instructor.com");
                userService.saveUser(instructor, instructorPassword);

                AppUser client = new AppUser();
                String clientPassword = "client";
                client.setRole(Role.CLIENT);
                client.setFirstName("Firstname");
                client.setLastName("Lastname");
                client.setEmail("client@client.com");
                userService.saveUser(client, clientPassword);

            }
        };
    }
}
