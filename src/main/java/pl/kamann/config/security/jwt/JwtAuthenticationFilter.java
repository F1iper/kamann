package pl.kamann.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AppUserRepository appUserRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, AppUserRepository appUserRepository) {
        this.jwtUtils = jwtUtils;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (token != null && jwtUtils.validateToken(token)) {
            String email = jwtUtils.extractEmail(token);

            try {
                AppUser user = appUserRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                List<GrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> {
                            String authorityName = "ROLE_" + role.getName();
                            return new SimpleGrantedAuthority(authorityName);
                        })
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}