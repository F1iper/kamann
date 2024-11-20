package pl.kamann.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sender")
@Getter
@Setter
public class SenderEmailProvider {

    private String email;
    private String password;
}