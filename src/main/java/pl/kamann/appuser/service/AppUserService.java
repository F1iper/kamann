package pl.kamann.appuser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.repository.AppUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;

    public AppUser createAppUser(AppUser appUser) {
        return repository.save(appUser);
    }

    public List<AppUser> getAllAppUsers() {
        return repository.findAll();
    }
}
