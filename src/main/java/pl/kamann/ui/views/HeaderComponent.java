package pl.kamann.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.session.SessionService;

public class HeaderComponent extends HorizontalLayout {

    public HeaderComponent(SessionService sessionService) {
        AppUser currentUser = sessionService.getCurrentUser();

        H1 title = new H1("Kalendarz");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        Avatar avatar = new Avatar(currentUser.getEmail());

        MenuBar menuBar = new MenuBar();
        MenuItem userItem = menuBar.addItem(avatar);
        SubMenu userSubMenu = userItem.getSubMenu();
        userSubMenu.addItem("Settings", e -> {/* Handle settings */});
        userSubMenu.addItem("Logout", e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().navigate("");
        });

        add(new DrawerToggle(), title, menuBar);
        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        expand(title);
        setWidthFull();
    }
}