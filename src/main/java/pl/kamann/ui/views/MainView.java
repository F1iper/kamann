package pl.kamann.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.appuser.service.AppUserService;

@Route("")
public class MainView extends VerticalLayout {
    private final AppUserService userService;

    public MainView(AppUserService userService) {
        this.userService = userService;

        H1 title = new H1("Booking App");
        Grid<AppUser> userGrid = new Grid<>(AppUser.class);
        userGrid.setItems(userService.getAllAppUsers());

        Button addUser = new Button("Add User");
        addUser.addClickListener(e -> showUserForm());

        add(title, addUser, userGrid);
    }

    private void showUserForm() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        TextField name = new TextField("Name");
        TextField email = new TextField("Email");

        Button save = new Button("Save", e -> {
            AppUser user = new AppUser();
            user.setName(name.getValue());
            user.setEmail(email.getValue());
            userService.createAppUser(user);
            dialog.close();
        });

        form.add(name, email, save);
        dialog.add(form);
        dialog.open();
    }
}