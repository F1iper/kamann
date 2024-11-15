package pl.kamann.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.session.SessionService;

@Route("instructor-dashboard")
public class InstructorDashboardView extends VerticalLayout {

    private SessionService sessionService;

    public InstructorDashboardView(SessionService sessionService) {
        this.sessionService = sessionService;

        add(new H1("Instructor Dashboard, user logged: " + sessionService.getCurrentUser().getFirstName()));
        Button logout = new Button("Logout", e -> {
            sessionService.removeCurrentUser();
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        add(logout);
    }
}
