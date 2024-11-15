package pl.kamann.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import pl.kamann.ui.views.specific.*;

public class DrawerConfigurator {

    public static Nav createDrawer() {
        Nav nav = new Nav();
        nav.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);

        nav.add(createNavigationItem("Kalendarz", "dashboard", CalendarView.class));
        nav.add(createNavigationItem("Instruktorzy", "instructors", InstructorsView.class));
        nav.add(createNavigationItem("Klienci", "clients", ClientsView.class));
        nav.add(createNavigationItem("Raporty", "reports", ReportsView.class));
        nav.add(createNavigationItem("Ustawienia", "settings", SettingsView.class));

        return nav;
    }

    private static RouterLink createNavigationItem(String label, String iconName, Class<? extends Component> view) {
        RouterLink link = new RouterLink();
        link.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.Gap.XSMALL,
                LumoUtility.Height.MEDIUM,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.XSMALL,
                LumoUtility.TextColor.BODY);

        Icon icon = new Icon("lumo", iconName);
        icon.addClassNames(LumoUtility.IconSize.MEDIUM);

        Span text = new Span(label);
        text.addClassNames(LumoUtility.FontWeight.MEDIUM);

        link.add(icon, text);
        link.setRoute(view);

        return link;
    }
}
