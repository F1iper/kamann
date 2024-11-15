package pl.kamann.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import pl.kamann.appuser.model.AppUser;

@Service
public class SessionService {

    private static final String USER_SESSION_KEY = "currentUser";

    private final HttpSession session;

    public SessionService(HttpSession session) {
        this.session = session;
    }

    public void setCurrentUser(AppUser user) {
        session.setAttribute(USER_SESSION_KEY, user);
    }

    public AppUser getCurrentUser() {
        return (AppUser) session.getAttribute(USER_SESSION_KEY);
    }

    public void removeCurrentUser() {
        session.removeAttribute(USER_SESSION_KEY);
        session.invalidate();
    }
}
