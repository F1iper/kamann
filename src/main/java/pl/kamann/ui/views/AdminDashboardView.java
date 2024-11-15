package pl.kamann.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.kamann.session.SessionService;

@Route("admin-dashboard")
@PageTitle("Admin Dashboard")
public class AdminDashboardView extends AppLayout {

    private final SessionService sessionService;

    public AdminDashboardView(SessionService sessionService) {
        this.sessionService = sessionService;

        addToNavbar(new HeaderComponent(sessionService));
        addToDrawer(DrawerConfigurator.createDrawer());

    }
}