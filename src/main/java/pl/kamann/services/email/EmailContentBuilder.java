package pl.kamann.services.email;

import java.util.Locale;
import java.util.ResourceBundle;

public class EmailContentBuilder {
    ResourceBundle bundle;
    public EmailContentBuilder(Locale locale){
        bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    public String getSubject() {
        return bundle.getString("registration.subject");
    }

    public String buildConfirmationEmail(String confirmationLink) {
        return bundle.getString("registration.message") +
                "<a href='" + confirmationLink + "'>" + confirmationLink + "</a>";
    }
}

