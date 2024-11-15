package pl.kamann.ui.views.specific;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.ui.views.AdminDashboardView;

@Route(value = "clients", layout = AdminDashboardView.class)
public class ClientsView extends VerticalLayout {
    public ClientsView() {
        add(new H2("Klienci"));
    }
}