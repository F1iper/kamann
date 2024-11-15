package pl.kamann.ui.views.specific;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.kamann.ui.views.AdminDashboardView;

@Route(value = "instructors", layout = AdminDashboardView.class)
public class InstructorsView extends VerticalLayout {
    public InstructorsView() {
        add(new H2("Instruktorzy"));
    }
}