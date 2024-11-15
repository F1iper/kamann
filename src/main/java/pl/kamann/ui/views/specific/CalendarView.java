package pl.kamann.ui.views.specific;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.ui.views.AdminDashboardView;

@Route(value = "dashboard", layout = AdminDashboardView.class)
public class CalendarView extends VerticalLayout {
    public CalendarView() {
        add(new H2("Kalendarz"));
    }
}