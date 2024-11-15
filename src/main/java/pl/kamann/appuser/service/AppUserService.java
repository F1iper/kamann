package pl.kamann.appuser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.repository.AppUserRepository;
import pl.kamann.session.SessionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;
    private final SessionService sessionService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AppUser createAppUser(AppUser appUser) {
        return repository.save(appUser);
    }

    public List<AppUser> getAllAppUsers() {
        return repository.findAll();
    }

    public AppUser getCurrentUser() {
        return sessionService.getCurrentUser();
    }

    public AppUser findByEmail(String email) {
        return repository.findByEmail(email);
    }
    public void saveUser(AppUser user, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        repository.save(user);
    }
}
