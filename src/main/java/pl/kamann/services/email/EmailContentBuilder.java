package pl.kamann.services.email;

import java.util.Locale;
import java.util.ResourceBundle;

public class EmailContentBuilder {
    ResourceBundle bundle;
    String key;
    public EmailContentBuilder(Locale locale, String key) {
        bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        this.key = key;
    }

    public String getSubject(String key) {
        return bundle.getString(key + ".subject");
    }

    public String buildConfirmationEmail(String confirmationLink) {
        return bundle.getString(key + ".message") +
                "<a href='" + confirmationLink + "'>" + confirmationLink + "</a>";
    }
}

