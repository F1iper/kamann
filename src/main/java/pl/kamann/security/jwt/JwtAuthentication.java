package pl.kamann.security.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final String email;

    public JwtAuthentication(String email, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.email = email;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // JWT itself is not stored here
    }

    @Override
    public Object getPrincipal() {
        return email;
    }
}