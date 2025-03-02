package pl.kamann.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
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

        String requestURI = request.getRequestURI();
        log.debug("🔍 JWT Filter Intercepted Request: {}", requestURI);

        if (requestURI.startsWith("/api/v1/auth/confirm") ||
                requestURI.startsWith("/api/v1/auth/register") ||
                requestURI.startsWith("/api/v1/auth/login")) {
            log.debug("Skipping JWT authentication for: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtils.extractTokenFromRequest(request);
        log.debug("🔍 Extracted JWT Token: {}", token);

        if (token == null || !jwtUtils.validateToken(token)) {
            log.debug("No valid JWT token found. Skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtils.extractEmail(token);

        try {
            AppUser user = appUserRepository.findAppUserWithRolesByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User with email {} not found", email);
                        return new UsernameNotFoundException("User not found");
                    });

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: {}", email);
        } catch (UsernameNotFoundException ex) {
            log.error("Authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            log.error("An error occurred during authentication: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}