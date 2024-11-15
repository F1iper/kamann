package pl.kamann.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.session.SessionService;

@Route("client-dashboard")
public class ClientDashboardView extends VerticalLayout {

    private SessionService sessionService;

    public ClientDashboardView() {
        add(new H1("Client Dashboard"));
        Button logout = new Button("Logout", e -> {
            sessionService.removeCurrentUser();
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        add(logout);
    }
}