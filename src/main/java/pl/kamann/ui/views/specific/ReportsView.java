package pl.kamann.ui.views.specific;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.ui.views.AdminDashboardView;

@Route(value = "reports", layout = AdminDashboardView.class)
public class ReportsView extends VerticalLayout {
    public ReportsView() {
        add(new H2("Raporty"));
        // Add your reports content here
    }
}