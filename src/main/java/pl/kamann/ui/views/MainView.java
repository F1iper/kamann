package pl.kamann.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.model.Role;
import pl.kamann.appuser.service.AppUserService;
import pl.kamann.session.SessionService;

@Route("")
public class MainView extends AppLayout {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public MainView(AppUserService appUserService, BCryptPasswordEncoder passwordEncoder, SessionService sessionService) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;

        AppUser currentUser = sessionService.getCurrentUser();

        if (currentUser != null) {
            navigateBasedOnRole(currentUser);
        } else {
            showLoginForm();
        }
    }

    private void showLoginForm() {
        FormLayout formLayout = new FormLayout();

        TextField emailField = new TextField("Email");
        PasswordField passwordField = new PasswordField("Password");
        Button loginButton = new Button("Login");

        loginButton.addClickListener(e -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();

            AppUser user = appUserService.findByEmail(email);

            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                sessionService.setCurrentUser(user);
                navigateBasedOnRole(sessionService.getCurrentUser());

            }});
        formLayout.add(emailField, passwordField, loginButton);
        setContent(formLayout);
    }

    private void navigateBasedOnRole(AppUser currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            getUI().ifPresent(ui -> ui.navigate("admin-dashboard"));
        } else if (currentUser.getRole() == Role.INSTRUCTOR) {
            getUI().ifPresent(ui -> ui.navigate("instructor-dashboard"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("client-dashboard"));
        }
    }
}
