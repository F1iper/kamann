package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ConfirmUserServiceTest {

    @Test
    void shouldConfirmAccount() {
        AppUser user = new AppUser();
        user.setConfirmationToken("test_token");

        assertEquals("test_token",  user.getConfirmationToken());
        assertFalse(user.isEnabled());
    }

//    @Test
//    void isAccountConfirmed() {
//        AppUser user = new AppUser();
//        user.setConfirmationToken("test_token");
//
//        when(appUserRepository.findByConfirmationToken("test_token")).thenReturn(Optional.of(user));
//        confirmUserService.confirmUserAccount(user.getConfirmationToken());
//
//        assertNull(user.getConfirmationToken());
//        assertTrue(user.isEnabled());
//    }

}
